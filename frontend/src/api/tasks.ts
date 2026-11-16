import { apiClient } from './client';
import type { CreateTaskRequest, EventTask, UpdateTaskRequest } from './types';

export async function getEventTasks(eventId: number): Promise<EventTask[]> {
  const { data } = await apiClient.get<EventTask[]>(`/events/${eventId}/tasks`);
  return data;
}

export async function createEventTask(eventId: number, body: CreateTaskRequest): Promise<EventTask> {
  const { data } = await apiClient.post<EventTask>(`/events/${eventId}/tasks`, body);
  return data;
}

export async function updateEventTask(
  eventId: number,
  taskId: number,
  body: UpdateTaskRequest,
): Promise<EventTask> {
  const { data } = await apiClient.patch<EventTask>(`/events/${eventId}/tasks/${taskId}`, body);
  return data;
}
