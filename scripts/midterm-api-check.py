"""API part of the mid-term manual test (T-99), see docs/qa/midterm-manual-test.md.

Runs W1-W5 against a running stack (API on :8080, frontend on :3000) with the dev seed users.
Usage: python3 scripts/midterm-api-check.py <results.json>
"""
import json, sys, time, threading, urllib.request, urllib.error
from datetime import datetime, timedelta, timezone

API = "http://localhost:8080/api/v1"
rows = []
tokens = {}


def call(method, path, token=None, body=None, base=API):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(base + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read().decode()
            return r.status, (json.loads(raw) if raw and raw[0] in "[{" else raw)
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw


def login(email):
    if email not in tokens:
        s, b = call("POST", "/auth/login", body={"email": email, "password": "password"})
        assert s == 200, (email, s, b)
        tokens[email] = b["accessToken"]
    return tokens[email]


def iso(dt):
    return dt.strftime("%Y-%m-%dT%H:%M:%SZ")


def check(wf, step, account, expected, status, body, ok=None):
    code = body.get("code") if isinstance(body, dict) else None
    actual = f"{status}" + (f" {code}" if code else "")
    passed = ok if ok is not None else actual.startswith(expected)
    rows.append((wf, step, account, expected, actual, "pass" if passed else "FAIL"))
    return body


U = lambda n: f"{n}@stud.ubbcluj.ro"
ADMIN, ANDREI, IOANA, ELENA, MARIA = "admin@ubbevents.ro", U("andrei.toma"), U("ioana.rusu"), U("elena.marin"), U("maria.dobre")
now = datetime.now(timezone.utc)
stamp = now.strftime("%H%M%S")

# ---------------- W1
s, org = call("POST", "/organizations", login(ADMIN), {"name": f"QA Club {stamp}", "description": None, "contactEmail": None, "presidentEmail": ANDREI})
check("W1", "Admin creates an organization with andrei.toma as president", "admin", "201", s, org)
oid = org["id"]
s, b = call("POST", "/organizations", login(IOANA), {"name": f"QA Other {stamp}", "presidentEmail": IOANA})
check("W1", "A student cannot create an organization", "ioana.rusu", "403", s, b)
s, b = call("POST", f"/organizations/{oid}/members", login(ANDREI), {"email": IOANA, "role": "MEMBER"})
check("W1", "President adds a MEMBER by email", "andrei.toma", "201", s, b)
s, b = call("POST", f"/organizations/{oid}/members", login(ANDREI), {"email": ELENA, "role": "ORGANIZER"})
check("W1", "President adds an ORGANIZER by email", "andrei.toma", "201", s, b)
s, b = call("POST", f"/organizations/{oid}/members", login(ANDREI), {"email": IOANA, "role": "MEMBER"})
check("W1", "Adding the same person twice is rejected", "andrei.toma", "409", s, b)
s, members = call("GET", f"/organizations/{oid}/members", login(IOANA))
check("W1", "A member sees the members list (3 people)", "ioana.rusu", "200", s, members, ok=s == 200 and len(members) == 3)
s, b = call("GET", f"/organizations/{oid}/members", login(MARIA))
check("W1", "An outsider cannot see the members list", "maria.dobre", "403", s, b)
ioana_m = next(m for m in members if m["email"] == IOANA)["membershipId"]
elena_m = next(m for m in members if m["email"] == ELENA)["membershipId"]
s, b = call("DELETE", f"/organizations/{oid}/members/{ioana_m}", login(ANDREI))
check("W1", "President removes the member", "andrei.toma", "204", s, b)
s, b = call("GET", f"/organizations/{oid}/members", login(ANDREI))
check("W1", "Members list has 2 people after the removal", "andrei.toma", "200", s, b, ok=s == 200 and len(b) == 2)

# ---------------- W2
def event_body(title, start, minutes=120, capacity=10, deadline=None):
    return {"title": title, "description": "QA", "location": "FSEGA", "category": "WORKSHOP",
            "startAt": iso(start), "endAt": iso(start + timedelta(minutes=minutes)), "capacity": capacity,
            "registrationDeadline": iso(deadline) if deadline else None}

s, ev = call("POST", f"/organizations/{oid}/events", login(ELENA), event_body(f"QA Main {stamp}", now + timedelta(days=3)))
check("W2", "Organizer creates a draft event", "elena.marin", "201", s, ev, ok=s == 201 and ev["status"] == "DRAFT")
eid = ev["id"]
s, b = call("POST", f"/organizations/{oid}/events", login(MARIA), event_body("x", now + timedelta(days=3)))
check("W2", "A non-member cannot create an event", "maria.dobre", "403", s, b)
s, b = call("POST", f"/organizations/{oid}/events", login(ELENA), {**event_body("Bad dates", now + timedelta(days=3)), "endAt": iso(now + timedelta(days=2))})
check("W2", "End before start is rejected", "elena.marin", "400", s, b)
s, b = call("GET", f"/events/{eid}", login(MARIA))
check("W2", "A draft is hidden from other users", "maria.dobre", "404", s, b)
s, b = call("PUT", f"/events/{eid}", login(ELENA), {**event_body(f"QA Main {stamp} (edited)", now + timedelta(days=3)), "version": ev["version"]})
check("W2", "Organizer edits the draft (version goes up)", "elena.marin", "200", s, b, ok=s == 200 and b["version"] == ev["version"] + 1)
s, b = call("PUT", f"/events/{eid}", login(ELENA), {**event_body("stale", now + timedelta(days=3)), "version": ev["version"]})
check("W2", "Edit with an old version is rejected", "elena.marin", "409 CONCURRENT_UPDATE", s, b)
s, b = call("POST", f"/events/{eid}/status", login(MARIA), {"status": "PUBLISHED"})
check("W2", "A non-organizer cannot publish", "maria.dobre", "403", s, b)
s, b = call("POST", f"/events/{eid}/status", login(ELENA), {"status": "PUBLISHED"})
check("W2", "Organizer publishes the event", "elena.marin", "200", s, b, ok=s == 200 and b["status"] == "PUBLISHED")
s, b = call("POST", f"/events/{eid}/status", login(ELENA), {"status": "ONGOING"})
check("W2", "Start 3 days early is rejected", "elena.marin", "409 INVALID_STATUS_TRANSITION", s, b)
s, ev2 = call("POST", f"/organizations/{oid}/events", login(ELENA), event_body(f"QA To cancel {stamp}", now + timedelta(days=4)))
s, b = call("POST", f"/events/{ev2['id']}/status", login(ELENA), {"status": "CANCELLED"})
check("W2", "Organizer cancels another event", "elena.marin", "200", s, b, ok=s == 200 and b["status"] == "CANCELLED")
s, b = call("GET", "/users/me/memberships", login(MARIA))
check("W2", "A member has no ORGANIZER/PRESIDENT role (Organizing menu hidden)", "maria.dobre", "200", s, b,
      ok=s == 200 and all(m["role"] == "MEMBER" for m in b))

# ---------------- W3
s, b = call("GET", f"/events?q=QA%20Main%20{stamp}", login(MARIA))
check("W3", "Text search finds the published event", "maria.dobre", "200", s, b, ok=s == 200 and b["totalElements"] == 1)
s, draft = call("POST", f"/organizations/{oid}/events", login(ELENA), event_body(f"QA Draft {stamp}", now + timedelta(days=5)))
s, b = call("GET", f"/events?q=QA%20Draft%20{stamp}", login(ELENA))
check("W3", "A DRAFT never appears in the search", "elena.marin", "200", s, b, ok=s == 200 and b["totalElements"] == 0)
s, b = call("GET", f"/events?q=QA%20To%20cancel%20{stamp}", login(MARIA))
check("W3", "A CANCELLED event does not appear in the search", "maria.dobre", "200", s, b, ok=s == 200 and b["totalElements"] == 0)
s, b = call("GET", f"/events?category=SPORTS&q=QA%20Main%20{stamp}", login(MARIA))
check("W3", "Category filter excludes the WORKSHOP event", "maria.dobre", "200", s, b, ok=s == 200 and b["totalElements"] == 0)
s, b = call("GET", "/events?category=NOT_A_CATEGORY", login(MARIA))
check("W3", "Unknown category is rejected", "maria.dobre", "400 INVALID_PARAMETER", s, b)
s, b = call("GET", f"/events/{eid}", login(MARIA))
check("W3", "Details show 10 free places", "maria.dobre", "200", s, b, ok=s == 200 and b["freePlaces"] == 10)

# ---------------- W4
s, b = call("POST", f"/events/{eid}/registrations", login(MARIA))
check("W4", "Student registers", "maria.dobre", "201", s, b)
s, b = call("GET", "/users/me/registrations", login(MARIA))
check("W4", "The registration is in My registrations", "maria.dobre", "200", s, b, ok=s == 200 and any(r["eventId"] == eid and r["status"] == "CONFIRMED" for r in b))
s, b = call("POST", f"/events/{eid}/registrations", login(MARIA))
check("W4", "Registering twice is rejected", "maria.dobre", "409 ALREADY_REGISTERED", s, b)
s, b = call("DELETE", f"/events/{eid}/registrations/me", login(MARIA))
check("W4", "Student cancels", "maria.dobre", "204", s, b)
s, b = call("POST", f"/events/{eid}/registrations", login(MARIA))
check("W4", "Student registers again", "maria.dobre", "201", s, b)
s, b = call("GET", f"/events/{eid}/registrations", login(ELENA))
check("W4", "Two rows kept: one CANCELLED, one CONFIRMED", "elena.marin", "200", s, b,
      ok=s == 200 and sorted(p["status"] for p in b) == ["CANCELLED", "CONFIRMED"])
s, full = call("POST", f"/organizations/{oid}/events", login(ELENA), event_body(f"QA Full {stamp}", now + timedelta(days=3), capacity=1))
call("POST", f"/events/{full['id']}/status", login(ELENA), {"status": "PUBLISHED"})
results = []
def reg(email):
    results.append(call("POST", f"/events/{full['id']}/registrations", login(email))[0])
for e in (IOANA, ANDREI):
    login(e)
threads = [threading.Thread(target=reg, args=(e,)) for e in (IOANA, ANDREI)]
[t.start() for t in threads]; [t.join() for t in threads]
check("W4", "Two students take the last place at the same time: one 201, one 409", "ioana.rusu + andrei.toma", "201 + 409", 0, {},
      ok=sorted(results) == [201, 409])
rows[-1] = rows[-1][:4] + (" + ".join(map(str, sorted(results))),) + rows[-1][5:]
s, b = call("POST", f"/events/{full['id']}/registrations", login(MARIA))
check("W4", "Full event is rejected", "maria.dobre", "409 EVENT_FULL", s, b)
call("POST", f"/events/{full['id']}/status", login(ELENA), {"status": "REGISTRATION_CLOSED"})
s, b = call("POST", f"/events/{full['id']}/registrations", login(MARIA))
check("W4", "Closed registration is rejected", "maria.dobre", "409 REGISTRATION_CLOSED", s, b)

# ---------------- W5
s, t = call("POST", f"/events/{eid}/tasks", login(ELENA), {"title": "Book the room", "dueAt": iso(now + timedelta(days=1)), "assigneeMembershipId": elena_m})
check("W5", "Organizer creates a task with an assignee (starts TODO)", "elena.marin", "201", s, t, ok=s == 201 and t["status"] == "TODO")
s, b = call("PATCH", f"/events/{eid}/tasks/{t['id']}", login(ELENA), {"status": "IN_PROGRESS"})
check("W5", "Organizer changes the task status", "elena.marin", "200", s, b, ok=s == 200 and b["status"] == "IN_PROGRESS")
s, b = call("POST", f"/events/{eid}/tasks", login(ELENA), {"title": "x", "assigneeMembershipId": ioana_m})
check("W5", "Assignee outside the organization is rejected", "elena.marin", "400", s, b)
s, b = call("GET", f"/events/{eid}/tasks", login(MARIA))
check("W5", "A participant cannot see the tasks", "maria.dobre", "403", s, b)
s, b = call("POST", f"/events/{eid}/announcements", login(ELENA), {"title": "Room change", "body": "We meet in room 2."})
check("W5", "Organizer posts an announcement", "elena.marin", "201", s, b)
s, b = call("GET", f"/events/{eid}/announcements", login(MARIA))
check("W5", "A registered participant reads it", "maria.dobre", "200", s, b, ok=s == 200 and len(b) == 1)
s, b = call("GET", f"/events/{eid}/announcements", login(U("radu.pop")))
check("W5", "An unrelated user cannot read it", "radu.pop", "403", s, b)
s, parts = call("GET", f"/events/{eid}/registrations", login(ELENA))
rid = next(p["registrationId"] for p in parts if p["status"] == "CONFIRMED")
s, b = call("POST", f"/events/{eid}/registrations/{rid}/check-in", login(ELENA))
check("W5", "Check-in while the event is PUBLISHED is rejected", "elena.marin", "409 CHECK_IN_NOT_ALLOWED", s, b)

# short event to go through start, check-in and complete
s, short = call("POST", f"/organizations/{oid}/events", login(ELENA), event_body(f"QA Short {stamp}", datetime.now(timezone.utc) + timedelta(seconds=40), minutes=1))
sid = short["id"]
call("POST", f"/events/{sid}/status", login(ELENA), {"status": "PUBLISHED"})
call("POST", f"/events/{sid}/registrations", login(MARIA))
s, b = call("POST", f"/events/{sid}/status", login(ELENA), {"status": "ONGOING"})
check("W2", "Organizer starts the event (within one hour of the start)", "elena.marin", "200", s, b, ok=s == 200 and b["status"] == "ONGOING")
s, parts = call("GET", f"/events/{sid}/registrations", login(ELENA))
rid = parts[0]["registrationId"]
s, b = call("POST", f"/events/{sid}/registrations/{rid}/check-in", login(ELENA))
check("W5", "Check-in at an ONGOING event", "elena.marin", "200", s, b, ok=s == 200 and b["checkedInAt"] is not None)
s, b = call("POST", f"/events/{sid}/registrations/{rid}/check-in", login(ELENA))
check("W5", "Second check-in is rejected", "elena.marin", "409 ALREADY_CHECKED_IN", s, b)
s, b = call("DELETE", f"/events/{sid}/registrations/me", login(MARIA))
check("W4", "A checked-in participant cannot cancel", "maria.dobre", "409 CANCEL_NOT_ALLOWED", s, b)
s, b = call("POST", f"/events/{sid}/status", login(ELENA), {"status": "COMPLETED"})
check("W2", "Complete before the end is rejected", "elena.marin", "409 INVALID_STATUS_TRANSITION", s, b)
end = datetime.fromisoformat(short["endAt"].replace("Z", "+00:00"))
time.sleep(max(0, (end - datetime.now(timezone.utc)).total_seconds()) + 2)
s, b = call("POST", f"/events/{sid}/status", login(ELENA), {"status": "COMPLETED"})
check("W2", "Organizer completes the event after its end", "elena.marin", "200", s, b, ok=s == 200 and b["status"] == "COMPLETED")
s, b = call("POST", f"/events/{sid}/announcements", login(ELENA), {"title": "x", "body": "y"})
check("W5", "No announcement on a COMPLETED event", "elena.marin", "409 ANNOUNCEMENT_NOT_ALLOWED", s, b)

# ---------------- errors and frontend
s, b = call("GET", "/users/me")
check("Errors", "Request without a token", "-", "401", s, b)
s, b = call("GET", "/users/me", "not-a-token")
check("Errors", "Request with an invalid token", "-", "401", s, b)
s, b = call("GET", "/events/999999", login(MARIA))
check("Errors", "Unknown event", "maria.dobre", "404", s, b)
for path in ("/", "/events", "/my-registrations", "/organizing"):
    s, b = call("GET", path, base="http://localhost:3000")
    check("Frontend", f"nginx serves the app at {path}", "-", "200", s, {})

json.dump(rows, open(sys.argv[1], "w"), indent=1)
print(f"{sum(r[5] == 'pass' for r in rows)} pass, {sum(r[5] == 'FAIL' for r in rows)} fail")
for r in rows:
    if r[5] == "FAIL":
        print("FAIL:", r)
