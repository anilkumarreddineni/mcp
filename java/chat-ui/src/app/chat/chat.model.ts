/** Body of `POST /api/chat` on mcp-client. */
export interface ChatRequest {
  message: string;
}

/** Response of `POST /api/chat`, also used for its error responses (400, 503). */
export interface ChatResponse {
  reply: string;
}

export type Sender = 'user' | 'bot';

export interface ChatMessage {
  sender: Sender;
  text: string;
  error?: boolean;
}
