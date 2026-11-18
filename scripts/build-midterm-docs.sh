#!/usr/bin/env bash
# Build the mid-term deliverables B, C, D, E as .docx and .pdf from docs/midterm/*.md.
#
# Usage:
#   scripts/build-midterm-docs.sh                 # all four, final
#   scripts/build-midterm-docs.sh B C             # only B and C
#   scripts/build-midterm-docs.sh --draft D E     # drafts: <name>-DRAFT.docx/.pdf, "DRAFT" on the title page
#
# Requirements: bash, git, Docker. All tools run in pinned Docker Hub images (nothing installed
# on the host). Intermediate files go to build/midterm-docs/ (git-ignored); outputs go to
# docs/midterm/export/. A final (non-draft) build only writes its outputs when the
# placeholder check passes; a draft build always writes them.
#
# Pipeline per document:
#   1. assemble Markdown from the sources (only the edits listed in "Content edits" below)
#   2. Mermaid blocks -> PNG (mermaid-cli), wireframe SVG -> PNG (rsvg-convert)
#   3. pandoc (gfm -> docx) with an embedded Lua filter: title page, links -> GitHub URLs,
#      figures with captions, table column widths
#   4. LibreOffice (headless, UNO script): styles, page numbers, landscape pages, table
#      formatting, image fitting, table of contents; saves the .docx and exports the .pdf
#   5. placeholder check on the text of the final .docx, page count of the .pdf
#
# Content edits made in the build (sources are not changed):
#   - "Index: [README](README.md)." removed from the "Part X of the mid-term deliverable." line
#   - relative links -> https://github.com/PatriciuMina/GenAI-Project/(blob|tree)/main/...
#   - horizontal rules (---) between sections dropped (headings start new pages instead)
#   - Mermaid code -> image with caption "Figure n. <nearest heading> — <diagram type>"
#   - B: the use-case overview diagram (4.3) is replaced by a link to it on GitHub (too large to print)
#   - B: appendix A = "ID schemes used across the files" table from docs/midterm/README.md
#   - C: wireframes after the five main product flows (6.6); appendix A = interview notes
#        I-01, I-02, I-03, I-05, I-06 (headings one level down; template README excluded)
#   - E: instructions block (What to write ... Done when) and "**Ticket:** ..." lines removed;
#        student headings numbered 8.1 ... 8.5
set -euo pipefail

# ---- pinned images -------------------------------------------------------------------------
IMG_PANDOC="pandoc/extra:3.6.4.0"
IMG_MERMAID="minlag/mermaid-cli:11.4.2"
IMG_OFFICE="gotenberg/gotenberg:8.20.1"   # Debian image with LibreOffice + python3-uno + fonts

REPO_URL="https://github.com/PatriciuMina/GenAI-Project"

ROOT="$(git -C "$(dirname "$0")" rev-parse --show-toplevel)"
MID="$ROOT/docs/midterm"
OUT="$ROOT/docs/midterm/export"
WORK="$ROOT/build/midterm-docs"
TOOLS="$WORK/tools"

DRAFT=0
DOCS=""
for a in "$@"; do
  case "$a" in
    --draft) DRAFT=1 ;;
    B|C|D|E) DOCS="$DOCS $a" ;;
    -h|--help) sed -n '2,12p' "$0"; exit 0 ;;
    *) echo "Unknown argument: $a (use B, C, D, E, --draft)" >&2; exit 2 ;;
  esac
done
[ -n "$DOCS" ] || DOCS="B C D E"

command -v docker >/dev/null || { echo "Docker is required" >&2; exit 1; }
docker info >/dev/null 2>&1 || { echo "Docker is not running" >&2; exit 1; }

# ---- facts read from the repository (fail if missing) --------------------------------------
COURSE="$(sed -n 's/^Course: //p' "$ROOT/docs/course/course-requirements.md" | head -1)"
[ -n "$COURSE" ] || { echo "Missing 'Course: ...' line in docs/course/course-requirements.md" >&2; exit 1; }
PROJECT="$(sed -n '1s/^# //p' "$ROOT/README.md")"
PROJECT_SUB="$(sed -n 's/^\(Event management system for student organizations at .*\)\.$/\1/p' "$ROOT/README.md" | head -1)"
[ -n "$PROJECT" ] && [ -n "$PROJECT_SUB" ] || { echo "Project name/description not found in README.md" >&2; exit 1; }
TEAM="$(sed -n 's/^## //p' "$MID/08-individual-contributions.md" | paste -sd ',' - | sed 's/,/, /g')"
[ "$(sed -n 's/^## //p' "$MID/08-individual-contributions.md" | wc -l | tr -d ' ')" = 5 ] || { echo "Expected 5 student sections in 08" >&2; exit 1; }
part_title() {  # "B. Documentation" from the mid-term table in course-requirements.md
  awk -F'|' -v p="$1" '$2 ~ "^ *" p "\\. " { s=$2; gsub(/^ +| +$/, "", s); print s; exit }' \
    "$ROOT/docs/course/course-requirements.md"
}

mkdir -p "$WORK" "$TOOLS" "$OUT"

# =============================================================================================
# Embedded tools
# =============================================================================================
cat > "$TOOLS/filter.lua" <<'LUA'
-- pandoc Lua filter: title page, GitHub links, figures, column widths.
local REPO = "https://github.com/PatriciuMina/GenAI-Project"
local S = function(m) return m and pandoc.utils.stringify(m) or "" end

local function png_size(path)
  local f = io.open(path, "rb"); if not f then return nil end
  local h = f:read(24); f:close()
  if not h or #h < 24 then return nil end
  local function u32(i) local a, b, c, d = h:byte(i, i + 3); return ((a * 256 + b) * 256 + c) * 256 + d end
  return u32(17), u32(21)
end

local function resolve(base, rel)
  local parts = {}
  for p in base:gmatch("[^/]+") do parts[#parts + 1] = p end
  local path, frag = rel:match("^([^#]*)(#?.*)$")
  local isdir = path:sub(-1) == "/"
  for p in path:gmatch("[^/]+") do
    if p == ".." then table.remove(parts) elseif p ~= "." then parts[#parts + 1] = p end
  end
  return table.concat(parts, "/"), isdir, frag
end

local inline_pass = {
  Image = function(img)
    local w = png_size(img.src)
    if w then
      local scale, factor = 3, 0.75            -- Mermaid PNGs are rendered at 3x
      if img.src:match("^wf%-") then scale, factor = 2, 1.0 end
      img.attributes.width = string.format("%.2fin", w / scale / 96 * factor)
    end
    return img
  end,
  Link = function(l)
    local t = l.target
    if t == "" or t:match("^#") or t:match("^%a[%w+.-]*:") then return nil end
    local p, isdir, frag = resolve("docs/midterm", t)
    l.target = REPO .. (isdir and "/tree/main/" or "/blob/main/") .. p .. frag
    io.stderr:write("LINK " .. t .. " -> " .. l.target .. "\n")
    return l
  end,
}

local block_pass = {
  HorizontalRule = function() return {} end,
  Para = function(p)
    if #p.content == 1 and p.content[1].t == "Image" then
      local img = p.content[1]
      return pandoc.Figure({ pandoc.Plain { img } }, { pandoc.Plain(img.caption) })
    end
  end,
}

local function words_max(s)
  local m = 0
  for w in s:gmatch("%S+") do local L = utf8.len(w) or #w; if L > m then m = L end end
  return m
end

local function set_widths(tbl, C)
  local n = #tbl.colspecs
  local st = {}
  for i = 1, n do st[i] = { sum = 0, cnt = 0, mx = 0, lw = 0 } end
  local function add(row)
    for i, cell in ipairs(row.cells) do
      if i <= n then
        local s = pandoc.utils.stringify(cell.contents)
        local L = utf8.len(s) or #s
        local x = st[i]
        x.sum, x.cnt = x.sum + L, x.cnt + 1
        if L > x.mx then x.mx = L end
        local lw = words_max(s); if lw > x.lw then x.lw = lw end
      end
    end
  end
  for _, r in ipairs(tbl.head.rows) do add(r) end
  for _, b in ipairs(tbl.bodies) do for _, r in ipairs(b.body) do add(r) end end
  local mins, des, smin, sdes = {}, {}, 0, 0
  for i = 1, n do
    local x = st[i]
    local avg = x.cnt > 0 and x.sum / x.cnt or 1
    mins[i] = (math.min(x.lw, 22) + 3) / C
    des[i] = math.sqrt(math.max(avg, 1) * math.max(x.mx, 1)) + 2
    smin, sdes = smin + mins[i], sdes + des[i]
  end
  local w = {}
  if smin >= 1 then
    for i = 1, n do w[i] = mins[i] / smin end
  else
    for i = 1, n do w[i] = mins[i] + (1 - smin) * des[i] / sdes end
  end
  for i = 1, n do tbl.colspecs[i] = { tbl.colspecs[i][1], w[i] } end
  return tbl
end

local function starts(s, prefix) return prefix ~= "" and s:sub(1, #prefix) == prefix end

local doc_pass = {
  Pandoc = function(doc)
    local m = doc.meta
    -- landscape ranges "start|end;start|end" (heading prefixes), shared with the UNO script
    local ranges = {}
    for r in S(m.xlandscape):gmatch("[^;]+") do
      local a, b = r:match("^(.-)|(.*)$"); ranges[#ranges + 1] = { a, b }
    end
    local inland, curend = false, nil
    for _, blk in ipairs(doc.blocks) do
      if blk.t == "Header" then
        local h = pandoc.utils.stringify(blk)
        if inland and curend and starts(h, curend) then inland = false end
        if not inland then
          for _, r in ipairs(ranges) do if starts(h, r[1]) then inland, curend = true, r[2] end end
        end
      elseif blk.t == "Table" then
        set_widths(blk, inland and 165 or 100)
      end
    end
    -- title page
    local function P(style, inl)
      return pandoc.Div({ pandoc.Para(inl) }, pandoc.Attr("", {}, { ["custom-style"] = style }))
    end
    local function T(s) return { pandoc.Str(s) } end
    local tp = {
      P("Title", T(S(m.xproject))),
      P("Subtitle", T(S(m.xprojectsub))),
      P("Subtitle", T("Mid-term deliverable — Part " .. S(m.xpart) .. (S(m.xdraft) == "1" and " (DRAFT)" or ""))),
      P("Author", T("Course: " .. S(m.xcourse))),
      P("Author", T("Team members:")),
    }
    for name in S(m.xteam):gmatch("[^,]+") do
      tp[#tp + 1] = P("Author", T((name:gsub("^%s+", ""))))
    end
    tp[#tp + 1] = P("Date", T("Date: " .. S(m.xdate)))
    tp[#tp + 1] = P("Date", { pandoc.Str("Repository: "), pandoc.Link(REPO, REPO) })
    tp[#tp + 1] = pandoc.Para(T("@@TOC@@"))
    local blocks = pandoc.List(tp)
    blocks:extend(doc.blocks)
    doc.blocks = blocks
    return doc
  end,
}

return { inline_pass, block_pass, doc_pass }
LUA

cat > "$TOOLS/check.lua" <<'LUA'
-- Reports empty table cells (to stderr) while pandoc writes the plain text.
local lastH, tableNo = "(start of document)", 0
return { {
  traverse = "topdown",
  Header = function(h) lastH = pandoc.utils.stringify(h) end,
  Table = function(t)
    tableNo = tableNo + 1
    local function chk(rows, kind)
      for r, row in ipairs(rows) do
        for c, cell in ipairs(row.cells) do
          if pandoc.utils.stringify(cell.contents):match("^%s*$") then
            io.stderr:write(string.format("EMPTY CELL: table %d after \"%s\", %s row %d, column %d\n",
              tableNo, lastH, kind, r, c))
          end
        end
      end
    end
    chk(t.head.rows, "header")
    for _, b in ipairs(t.bodies) do chk(b.body, "body") end
  end,
} }
LUA

cat > "$TOOLS/post.py" <<'PY'
# LibreOffice UNO post-processing: styles, page styles, landscape ranges, tables, images, TOC.
import json, re, sys, time
import uno
from com.sun.star.beans import PropertyValue

cfg = json.load(open(sys.argv[1], encoding="utf-8"))

def pv(n, v):
    p = PropertyValue(); p.Name = n; p.Value = v; return p

CENTER = uno.Enum("com.sun.star.style.ParagraphAdjust", "CENTER")
LEFT = uno.Enum("com.sun.star.style.ParagraphAdjust", "LEFT")
PAGE_BEFORE = uno.Enum("com.sun.star.style.BreakType", "PAGE_BEFORE")
FONT, MONO = "Calibri", "Courier New"

local = uno.getComponentContext()
resolver = local.ServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", local)
ctx = None
for _ in range(240):
    try:
        ctx = resolver.resolve("uno:socket,host=127.0.0.1,port=2002;urp;StarOffice.ComponentContext")
        break
    except Exception:
        time.sleep(0.5)
if ctx is None:
    sys.exit("LibreOffice did not start")
desktop = ctx.ServiceManager.createInstanceWithContext("com.sun.star.frame.Desktop", ctx)
doc = desktop.loadComponentFromURL(uno.systemPathToFileUrl(cfg["in"]), "_blank", 0, (pv("Hidden", True),))

fam = doc.StyleFamilies
ps, cs, pgs = fam.getByName("ParagraphStyles"), fam.getByName("CharacterStyles"), fam.getByName("PageStyles")
def pstyle(n): return ps.getByName(n) if ps.hasByName(n) else None

# ---- fonts and paragraph styles ----
for n in ps.ElementNames:
    s = ps.getByName(n)
    try:
        s.CharFontName = MONO if n in ("Source Code", "Preformatted Text") else FONT
    except Exception:
        pass
if cs.hasByName("Verbatim Char"):
    cs.getByName("Verbatim Char").CharFontName = MONO
base = pstyle("Standard"); base.CharHeight = 10.5
for n in ("Text body", "Body Text", "First Paragraph", "Compact"):
    s = pstyle(n)
    if s: s.CharHeight = 10.5; s.ParaBottomMargin = 160
for lvl, size in ((1, 18), (2, 14), (3, 12), (4, 11)):
    s = pstyle("Heading %d" % lvl)
    if s:
        s.CharHeight = size; s.CharWeight = 150.0; s.CharPosture = uno.Enum("com.sun.star.awt.FontSlant", "NONE")
        s.CharColor = 0x1F3864; s.ParaTopMargin = 420 if lvl <= 2 else 300; s.ParaBottomMargin = 160
        s.ParaKeepTogether = True
h1 = pstyle("Heading 1"); h1.BreakType = PAGE_BEFORE
for n, size, bold, top in (("Title", 30, True, 4500), ("Subtitle", 15, False, 300), ("Author", 12, False, 120), ("Date", 12, False, 120)):
    s = pstyle(n)
    if s:
        s.CharHeight = size; s.CharWeight = 150.0 if bold else 100.0; s.ParaAdjust = CENTER
        s.ParaTopMargin = top; s.CharColor = 0x1F3864 if n == "Title" else 0x000000
for n in ("Image Caption", "Table Caption"):
    s = pstyle(n)
    if s: s.ParaAdjust = CENTER; s.CharHeight = 9; s.CharPosture = uno.Enum("com.sun.star.awt.FontSlant", "ITALIC")
for n in ("Captioned Figure", "Figure"):
    s = pstyle(n)
    if s: s.ParaAdjust = CENTER

# ---- page styles ----
paras = []
en = doc.Text.createEnumeration()
while en.hasMoreElements():
    el = en.nextElement()
    if el.supportsService("com.sun.star.text.Paragraph"):
        paras.append(el)
base_page = paras[0].PageStyleName

def setup_page(st, landscape, footer):
    st.IsLandscape = landscape
    st.Width, st.Height = (29700, 21000) if landscape else (21000, 29700)
    st.LeftMargin = st.RightMargin = 2000
    st.TopMargin, st.BottomMargin = 1800, 1400
    st.FooterIsOn = footer
    if not footer:
        return
    st.FooterBodyDistance = 400
    ft = st.FooterText
    ft.setString("")
    cur = ft.createTextCursor()
    ft.insertString(cur, cfg["footer"] + "  ·  Page ", False)
    f1 = doc.createInstance("com.sun.star.text.TextField.PageNumber")
    f1.NumberingType = 4; f1.SubType = uno.Enum("com.sun.star.text.PageNumberType", "CURRENT")
    ft.insertTextContent(cur, f1, False)
    ft.insertString(cur, " of ", False)
    f2 = doc.createInstance("com.sun.star.text.TextField.PageCount"); f2.NumberingType = 4
    ft.insertTextContent(cur, f2, False)
    c = ft.createTextCursor(); c.gotoStart(False); c.gotoEnd(True)
    c.CharHeight = 9; c.CharFontName = FONT; c.ParaAdjust = CENTER; c.CharColor = 0x595959

def new_page_style(name):
    if not pgs.hasByName(name):
        pgs.insertByName(name, doc.createInstance("com.sun.star.style.PageStyle"))
    return pgs.getByName(name)

setup_page(pgs.getByName(base_page), False, True)
title_page = new_page_style("TitlePage"); setup_page(title_page, False, False); title_page.FollowStyle = base_page
land = new_page_style("Landscape"); setup_page(land, True, True)
paras[0].PageDescName = "TitlePage"

# ---- landscape ranges (heading prefixes) ----
inland, cur_end, report = False, None, []
for p in paras:
    if not p.ParaStyleName.startswith("Heading"):
        continue
    h = p.getString()
    if inland and cur_end and h.startswith(cur_end):
        p.PageDescName = base_page; inland = False; report.append("portrait from: " + h)
    if not inland:
        for a, b in cfg["landscape"]:
            if h.startswith(a):
                p.PageDescName = "Landscape"; inland, cur_end = True, b; report.append("landscape from: " + h)
                break

# ---- table of contents at the marker ----
marker = [p for p in paras if p.getString() == "@@TOC@@"][0]
marker.setString("")
marker.PageDescName = base_page
idx = doc.createInstance("com.sun.star.text.ContentIndex")
idx.CreateFromOutline = True
idx.Level = int(cfg["toclevel"])
idx.Title = "Contents"
doc.Text.insertTextContent(marker.getStart(), idx, False)
for n in ("Contents Heading",):
    s = pstyle(n)
    if s: s.CharHeight = 18; s.CharWeight = 150.0; s.CharColor = 0x1F3864

# ---- tables ----
line = uno.createUnoStruct("com.sun.star.table.BorderLine2")
line.Color = 0xA6A6A6; line.LineWidth = 18; line.OuterLineWidth = 18
tables = doc.TextTables
for i in range(tables.Count):
    t = tables.getByIndex(i)
    ncols = t.Columns.Count
    size = 8 if ncols >= 6 else 9
    t.RepeatHeadline = True
    try: t.HeaderRowCount = 1
    except Exception: pass
    t.HoriOrient = 6  # FULL: span the text area of the page (portrait or landscape)
    rows = t.Rows
    for r in range(rows.Count):
        rows.getByIndex(r).IsSplitAllowed = False
    tb = t.TableBorder2
    for side in ("TopLine", "BottomLine", "LeftLine", "RightLine", "HorizontalLine", "VerticalLine"):
        setattr(tb, side, line); setattr(tb, "Is" + side + "Valid", True)
    tb.Distance = 90; tb.IsDistanceValid = True
    t.TableBorder2 = tb
    for name in t.getCellNames():
        cell = t.getCellByName(name)
        c = cell.createTextCursor(); c.gotoStart(False); c.gotoEnd(True)
        c.CharHeight = size; c.ParaTopMargin = 0; c.ParaBottomMargin = 40; c.ParaAdjust = LEFT
        mm = re.match(r"^[A-Z]+(\d+)$", name)
        header = bool(mm and mm.group(1) == "1")
        if header:
            cell.BackColor = 0xDCE3F0
        pe = cell.getText().createEnumeration()
        while pe.hasMoreElements():
            par = pe.nextElement()
            te = par.createEnumeration()
            while te.hasMoreElements():
                portion = te.nextElement()
                code = portion.CharStyleName == "Verbatim Char"
                portion.CharFontName = MONO if code else FONT
                if header: portion.CharWeight = 150.0

# ---- images: fit into the text area of their page, center ----
doc.refresh()
gos = doc.GraphicObjects
img_report = []
for i in range(gos.Count):
    g = gos.getByIndex(i)
    a = g.getAnchor()
    cur = a.getText().createTextCursorByRange(a)
    try:
        st = pgs.getByName(cur.PageStyleName)
    except Exception:
        st = pgs.getByName(base_page)
    maxw = st.Width - st.LeftMargin - st.RightMargin
    maxh = st.Height - st.TopMargin - st.BottomMargin - 2600
    if "wireframe" in (g.Description or "").lower():
        maxh = int(maxh * 0.6)
    s = g.Size
    k = min(1.0, maxw / s.Width, maxh / s.Height)
    if k < 1.0:
        s.Width, s.Height = int(s.Width * k), int(s.Height * k); g.Size = s
    cur.ParaAdjust = CENTER
    cur.ParaKeepTogether = True  # keep the image with its caption (next paragraph)
    img_report.append("%s: %.1f x %.1f cm on %s" % ((g.Description or "")[:60], s.Width / 1000, s.Height / 1000, st.Name))

# ---- metadata, indexes, save ----
dp = doc.DocumentProperties
dp.Title = cfg["doctitle"]; dp.Subject = cfg["course"]; dp.Author = cfg["team"]
for _ in range(2):
    doc.refresh()
    idxs = doc.DocumentIndexes
    for i in range(idxs.Count):
        idxs.getByIndex(i).update()
    doc.TextFields.refresh()

doc.storeToURL(uno.systemPathToFileUrl(cfg["out_docx"]), (pv("FilterName", "MS Word 2007 XML"), pv("Overwrite", True)))
doc.storeToURL(uno.systemPathToFileUrl(cfg["out_pdf"]), (pv("FilterName", "writer_pdf_Export"), pv("Overwrite", True)))
doc.close(True)
with open(cfg["report"], "w", encoding="utf-8") as f:
    f.write("\n".join(report + img_report) + "\n")
PY

cat > "$TOOLS/svg2png.py" <<'PY'
# Renders /data/wf-*.svg at 2x with headless Chromium (Arial available in this image).
# Chromium is given a taller window than the SVG; the PNG is then cut to the SVG height.
# Cutting rows off the bottom of a PNG is safe without decoding: scanline filters only
# reference earlier rows.
import glob, os, re, struct, subprocess, sys, zlib
SCALE = 2
def chunks(data):
    i = 8
    while i < len(data):
        n = struct.unpack(">I", data[i:i + 4])[0]
        yield data[i + 4:i + 8], data[i + 8:i + 8 + n]
        i += 12 + n
def chunk(t, d):
    return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
def crop_height(path, rows):
    data = open(path, "rb").read()
    ihdr, idat, other = None, b"", []
    for t, d in chunks(data):
        if t == b"IHDR": ihdr = d
        elif t == b"IDAT": idat += d
        elif t not in (b"IEND",): other.append((t, d))
    w, h, depth, ctype = struct.unpack(">IIBB", ihdr[:10])
    assert depth == 8 and ihdr[12] == 0, "unexpected PNG format"
    bpp = {2: 3, 6: 4, 0: 1, 4: 2}[ctype]
    raw = zlib.decompress(idat)[: rows * (1 + w * bpp)]
    ihdr = struct.pack(">II", w, rows) + ihdr[8:]
    out = data[:8] + chunk(b"IHDR", ihdr) + b"".join(chunk(t, d) for t, d in other if t != b"IHDR")
    out += chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    open(path, "wb").write(out)
for svg in sorted(glob.glob(os.path.join(sys.argv[1], "wf-*.svg"))):
    tag = open(svg, encoding="utf-8").read()
    tag = tag[tag.index("<svg"):]; tag = tag[: tag.index(">")]
    w = int(re.search(r' width="(\d+)"', tag).group(1)); h = int(re.search(r' height="(\d+)"', tag).group(1))
    html, png = svg[:-4] + ".html", svg[:-4] + ".png"
    open(html, "w").write('<html><body style="margin:0;background:#fff"><img src="%s" style="display:block;width:%dpx;height:%dpx"></body></html>' % (os.path.basename(svg), w, h))
    cmd = ["chromium", "--headless", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
           "--force-device-scale-factor=%d" % SCALE, "--window-size=%d,%d" % (w, h + 300),
           "--screenshot=" + png, "file://" + html]
    for attempt in range(3):  # headless Chromium occasionally hangs: time out and retry
        try:
            subprocess.run(cmd, check=True, capture_output=True, timeout=90)
            break
        except (subprocess.TimeoutExpired, subprocess.CalledProcessError) as e:
            print("chromium attempt %d for %s failed: %s" % (attempt + 1, os.path.basename(svg), type(e).__name__), file=sys.stderr)
            subprocess.run(["pkill", "-f", "chromium"], capture_output=True)
    else:
        sys.exit("chromium failed 3 times for " + svg)
    crop_height(png, h * SCALE)
PY

# Mermaid rendering config (not diagram content): measure text with a font that exists in the
# image and use SVG text labels, so subgraph titles are not clipped.
cat > "$TOOLS/mermaid.json" <<'JSON'
{"theme": "default", "fontFamily": "DejaVu Sans", "themeVariables": {"fontFamily": "DejaVu Sans"},
 "flowchart": {"htmlLabels": false}}
JSON

# =============================================================================================
# Assembly helpers (POSIX awk/sed; macOS bash 3.2 compatible)
# =============================================================================================
strip_index() { sed -E 's/ Index: \[README\]\(README\.md\)\.//' "$1"; }

assemble_B() {
  for f in 01-problem-statement 02-requirements 03-actors-roles 04-use-cases 05-workflows; do
    strip_index "$MID/$f.md"; printf '\n\n'
  done
  printf '# Appendix A. ID schemes used across the files\n\n'
  awk '/^### ID schemes used across the files/{on=1; next} /^## /{on=0} on' "$MID/README.md"
}

assemble_C() {
  # wireframes after the "Implemented by:" line of the five main product flows (6.6)
  local wf="student-event-details student-events-desktop student-my-registrations organizer-event-page mobile-check-in"
  local k=0 caps=""
  for f in $wf; do
    k=$((k + 1))
    local alt
    alt="$(sed -n "s/^!\[\([^]]*\)\](\.\.\/assets\/wireframes\/$f\.svg)$/\1/p" "$ROOT/docs/design/wireframes.md" | head -1)"
    [ -n "$alt" ] || { echo "No wireframe caption for $f in docs/design/wireframes.md" >&2; exit 1; }
    caps="$caps$k|$f|$alt;"
  done
  strip_index "$MID/06-ux-research.md" | awk -v caps="$caps" '
    BEGIN { n = split(caps, L, ";"); for (i = 1; i <= n; i++) if (L[i] != "") { split(L[i], p, "|"); F[p[1]] = p[2]; A[p[1]] = p[3] } }
    /^## 6\.6 / { in66 = 1 } /^## 6\.7 / { in66 = 0 }
    { print }
    in66 && /^   Implemented by:/ { k++; printf "\n   ![Figure %d. %s (docs/assets/wireframes/%s.svg)](wf-%d.png)\n\n", k, A[k], F[k], k }'
  printf '\n\n# Appendix A. Interview notes\n\n'
  for i in I-01 I-02 I-03 I-05 I-06; do
    sed -E 's/^(#+) /#\1 /' "$MID/interviews/$i.md"; printf '\n\n'
  done
}

assemble_D() { strip_index "$MID/07-genai-report.md"; }

assemble_E() {
  strip_index "$MID/08-individual-contributions.md" | awk '
    /^\*\*What to write/ { skip = 1 }
    skip && /^---$/ { skip = 0 }
    skip { next }
    /^\*\*Ticket:\*\*/ { next }
    /^## / { n++; sub(/^## /, "## 8." n " ") }
    { print }'
}

# Mermaid blocks -> diag-N.mmd + image line with caption from the nearest heading
extract_mermaid() {  # $1 = work dir; stdin -> stdout
  awk -v dir="$1" '
    !inm && /^#+ / { h = $0; sub(/^#+ +/, "", h) }
    # 4.3: the overview diagram is too large for a printed page -> link to GitHub instead
    /^```mermaid/ && h ~ /^4\.3 Use case overview diagram/ { skip = 1; next }
    skip && /^```/ { skip = 0
      print "The use-case overview diagram is too large to be readable on a printed page. View it on GitHub: https://github.com/PatriciuMina/GenAI-Project/blob/main/docs/midterm/04-use-cases.md#43-use-case-overview-diagram."
      next }
    skip { next }
    /^```mermaid/ { inm = 1; n++; f = dir "/diag-" n ".mmd"; first = 1; next }
    inm && /^```/ {
      inm = 0; close(f)
      cap = "Figure " n ". " h
      if (tolower(h) !~ /diagram/) cap = cap " — " t
      printf "![%s](diag-%d.png)\n", cap, n
      next
    }
    inm {
      if (first) { t = $1; if (t ~ /^sequence/) t = "sequence diagram"; else if (t ~ /^state/) t = "state diagram"; else t = "flowchart"; first = 0 }
      print > f; next
    }
    { print }'
}

json_str() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }

# =============================================================================================
build_doc() {
  local X="$1" name sources toc land
  case "$X" in
    B) name="B-technical-document"; toc=3
       sources="01-problem-statement.md 02-requirements.md 03-actors-roles.md 04-use-cases.md 05-workflows.md README.md"
       land="2. Requirements|3. Actors and roles;3.2 Permission matrix|4.3 Use case overview" ;;
    C) name="C-ux-research"; toc=2
       sources="06-ux-research.md interviews/I-01.md interviews/I-02.md interviews/I-03.md interviews/I-05.md interviews/I-06.md"
       land="6.3 Interview log|6.6 Main product flows" ;;
    D) name="D-genai-report"; toc=2; sources="07-genai-report.md"; land="7. GenAI declaration|7.5 How" ;;
    E) name="E-individual-contributions"; toc=2; sources="08-individual-contributions.md"; land="" ;;
  esac
  [ "$DRAFT" = 1 ] && name="$name-DRAFT"
  local W="$WORK/$X" part date
  part="$(part_title "$X")"
  [ -n "$part" ] || { echo "Part $X not found in course-requirements.md" >&2; exit 1; }
  date="$(cd "$MID" && git log -1 --format=%cs -- $sources)"
  echo "== $X: $name ($part, date $date)"
  rm -rf "$W"; mkdir -p "$W"

  "assemble_$X" | extract_mermaid "$W" > "$W/doc.md"

  if ls "$W"/diag-*.mmd >/dev/null 2>&1; then
    echo "   Mermaid: $(ls "$W"/diag-*.mmd | wc -l | tr -d ' ') diagrams"
    docker run --rm -v "$W":/data -v "$TOOLS":/tools --entrypoint sh "$IMG_MERMAID" -c \
      'for f in /data/diag-*.mmd; do /home/mermaidcli/node_modules/.bin/mmdc -p /puppeteer-config.json -q -c /tools/mermaid.json -i "$f" -o "${f%.mmd}.png" -s 3 -b white || exit 1; done'
  fi
  if [ "$X" = C ]; then
    local k=0
    for f in student-event-details student-events-desktop student-my-registrations organizer-event-page mobile-check-in; do
      k=$((k + 1)); cp "$ROOT/docs/assets/wireframes/$f.svg" "$W/wf-$k.svg"
    done
    # headless Chromium from the LibreOffice image (has Arial, the font the SVGs use), 2x
    docker run --rm -u 0 -v "$W":/data -v "$TOOLS":/tools --entrypoint python3 "$IMG_OFFICE" /tools/svg2png.py /data \
      || { echo "Wireframe rendering failed" >&2; exit 1; }
  fi

  docker run --rm -v "$W":/data -v "$TOOLS":/tools "$IMG_PANDOC" doc.md -f gfm -t docx \
    --lua-filter /tools/filter.lua -o pandoc.docx \
    -M xproject="$PROJECT" -M xprojectsub="$PROJECT_SUB" -M xpart="$part" -M xcourse="$COURSE" \
    -M xteam="$TEAM" -M xdate="$date" -M xdraft="$DRAFT" -M xlandscape="$land" 2> "$W/pandoc.log" \
    || { cat "$W/pandoc.log" >&2; exit 1; }

  local landjson="[" first=1 r
  local IFS_OLD="$IFS"; IFS=';'
  for r in $land; do
    [ $first = 1 ] || landjson="$landjson,"; first=0
    landjson="$landjson[\"$(json_str "${r%%|*}")\",\"$(json_str "${r#*|}")\"]"
  done
  IFS="$IFS_OLD"; landjson="$landjson]"
  cat > "$W/post.json" <<JSON
{"in": "/data/pandoc.docx", "out_docx": "/data/$name.docx", "out_pdf": "/data/$name.pdf",
 "report": "/data/layout.txt", "landscape": $landjson, "toclevel": $toc,
 "footer": "$(json_str "$PROJECT") · Part $X", "doctitle": "$(json_str "$PROJECT — Mid-term deliverable, Part $part")",
 "course": "$(json_str "$COURSE")", "team": "$(json_str "$TEAM")"}
JSON
  docker run --rm -u 0 -v "$W":/data -v "$TOOLS":/tools --entrypoint sh "$IMG_OFFICE" -c '
    export HOME=/tmp
    rc=1
    # The Python-UNO bridge occasionally crashes or hangs while soffice is starting:
    # each attempt gets 3 minutes, then soffice is stopped and started fresh (max 3 attempts).
    for attempt in 1 2 3; do
      soffice --headless --invisible --nologo --norestore --nodefault "-env:UserInstallation=file:///tmp/lo" \
        "--accept=socket,host=127.0.0.1,port=2002;urp;" >/dev/null 2>&1 &
      sleep 8
      timeout -k 10 180 python3 /tools/post.py /data/post.json && { rc=0; break; }
      echo "   LibreOffice attempt $attempt failed or timed out after 3 minutes, retrying" >&2
      pkill -9 -f soffice; rm -rf /tmp/lo; sleep 3
    done
    pkill -f soffice
    [ $rc = 0 ] && qpdf --show-npages /data/'"$name"'.pdf > /data/pages.txt
    exit $rc' || { echo "LibreOffice step failed" >&2; exit 1; }

  # ---- checks on the generated document text ----
  docker run --rm -v "$W":/data -v "$TOOLS":/tools "$IMG_PANDOC" "$name.docx" -f docx -t plain --wrap=none \
    --lua-filter /tools/check.lua -o text.txt 2> "$W/empty-cells.txt" || true
  local pat='TODO|TBD|[Pp]ending|[Ff]ill in|[Tt]o be (added|confirmed)|<[^ =]|[Ll]orem|XXX|What to write|Done when|Example – replace|none yet'
  local ok_pat='FR-<group>-NN|TODO,? (/ )?IN_PROGRESS|(as|status) TODO|TODO\.$|TODO, IN_PROGRESS'
  grep -n -E "$pat" "$W/text.txt" > "$W/hits-all.txt" || true
  # open decisions and table cells that end mid-sentence (";") in the assembled Markdown
  grep -n -i -E 'has to decide|to be decided|;[[:space:]]*\|[[:space:]]*$' "$W/doc.md" | sed 's/^/md:/' >> "$W/hits-all.txt" || true
  grep -E "$ok_pat" "$W/hits-all.txt" > "$W/hits-accepted.txt" || true
  grep -v -E "$ok_pat" "$W/hits-all.txt" > "$W/hits.txt" || true
  local nh na ne pages
  nh=$(wc -l < "$W/hits.txt" | tr -d ' '); na=$(wc -l < "$W/hits-accepted.txt" | tr -d ' ')
  ne=$(grep -c '^EMPTY CELL' "$W/empty-cells.txt" || true); pages=$(cat "$W/pages.txt")
  {
    echo "### $name"
    echo "pages: $pages"
    echo "figures: $(grep -c '!\[Figure' "$W/doc.md" || true)"
    echo "tables: $(grep -c '^| *-' "$W/doc.md" || true)"
    echo "placeholder hits: $nh"; sed 's/^/  HIT /' "$W/hits.txt"
    echo "accepted hits (ID notation / task status TODO): $na"; sed 's/^/  ok  /' "$W/hits-accepted.txt"
    echo "empty table cells: $ne"; grep '^EMPTY CELL' "$W/empty-cells.txt" | sed 's/^/  /' || true
    echo "table cells that are only '—' (accepted): $(grep -o '| — |' "$W/doc.md" | wc -l | tr -d ' ')"
    echo "layout:"; sed 's/^/  /' "$W/layout.txt"
    grep '^LINK' "$W/pandoc.log" | sed 's/^/  /' || true
  } > "$W/report.txt"
  cat "$W/report.txt"

  if [ "$DRAFT" = 0 ] && { [ "$nh" != 0 ] || [ "$ne" != 0 ]; }; then
    echo "!! $name: placeholder check failed; outputs NOT copied to docs/midterm/export/ (see $W)" >&2
    FAILED="$FAILED $X"
    return 0
  fi
  cp "$W/$name.docx" "$W/$name.pdf" "$OUT/"
  echo "   -> docs/midterm/export/$name.docx, $name.pdf"
}

FAILED=""
echo "Images: $IMG_PANDOC, $IMG_MERMAID, $IMG_OFFICE"
for X in $DOCS; do build_doc "$X"; done
cat "$WORK"/*/report.txt > "$WORK/report.txt" 2>/dev/null || true
if [ -n "$FAILED" ]; then echo "Failed placeholder check:$FAILED" >&2; exit 1; fi
echo "Done. Full report: build/midterm-docs/report.txt"
