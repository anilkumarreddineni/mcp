import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, throwError } from 'rxjs';

import { ChatRequest, ChatResponse } from './chat.model';

export const UNREACHABLE_REPLY = "Can't reach the chat service. Check your connection and try again.";
export const GENERIC_ERROR_REPLY = 'Something went wrong. Please try again.';

/**
 * Sends chat messages to mcp-client, which asks Claude and calls the MCP tools.
 * mcp-client keeps one conversation per HTTP session (cookie), so follow-up
 * questions keep their context. In development the Angular dev server proxies
 * `/api` to mcp-client (see proxy.conf.json), so the cookie is same-origin.
 */
@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);

  /** Emits Claude's reply, or errors with a message that is safe to show the user. */
  send(message: string): Observable<string> {
    const body: ChatRequest = { message };
    return this.http.post<ChatResponse>('/api/chat', body).pipe(
      map((response) => response.reply),
      catchError((error: HttpErrorResponse) => throwError(() => new Error(toUserMessage(error)))),
    );
  }
}

function toUserMessage(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return UNREACHABLE_REPLY;
  }
  // mcp-client returns a readable reply for 400 (blank message) and 503 (MCP server or Claude down)
  const reply = (error.error as Partial<ChatResponse> | null)?.reply;
  return reply || GENERIC_ERROR_REPLY;
}
