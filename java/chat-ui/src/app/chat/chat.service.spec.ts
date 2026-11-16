import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { ChatService, GENERIC_ERROR_REPLY, UNREACHABLE_REPLY } from './chat.service';

describe('ChatService', () => {
  let service: ChatService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideZonelessChangeDetection(), provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ChatService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('posts the message and returns the reply', () => {
    let reply: string | undefined;
    service.send('Status of 1003?').subscribe((r) => (reply = r));

    const request = http.expectOne('/api/chat');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ message: 'Status of 1003?' });
    request.flush({ reply: 'Order 1003 has shipped.' });

    expect(reply).toBe('Order 1003 has shipped.');
  });

  it('uses the server reply from an error response', () => {
    let error: Error | undefined;
    service.send('Show me all orders').subscribe({ error: (e: Error) => (error = e) });

    http
      .expectOne('/api/chat')
      .flush({ reply: "Sorry, I can't reach the order system" }, { status: 503, statusText: 'Unavailable' });

    expect(error?.message).toBe("Sorry, I can't reach the order system");
  });

  it('falls back to a generic message when the error has no reply', () => {
    let error: Error | undefined;
    service.send('hi').subscribe({ error: (e: Error) => (error = e) });

    http.expectOne('/api/chat').flush('oops', { status: 500, statusText: 'Server Error' });

    expect(error?.message).toBe(GENERIC_ERROR_REPLY);
  });

  it('reports when mcp-client cannot be reached', () => {
    let error: Error | undefined;
    service.send('hi').subscribe({ error: (e: Error) => (error = e) });

    http.expectOne('/api/chat').error(new ProgressEvent('error'), { status: 0 });

    expect(error?.message).toBe(UNREACHABLE_REPLY);
  });
});
