import {
  afterRenderEffect,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ChatMessage } from './chat.model';
import { ChatService } from './chat.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat {
  private readonly chatService = inject(ChatService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly suggestions = [
    "What's the status of order 1003?",
    'Show me all orders',
    'Which orders have shipped?',
  ];

  protected readonly messages = signal<ChatMessage[]>([
    { sender: 'bot', text: 'Hi! I can look up orders and their status. What would you like to know?' },
  ]);
  protected readonly draft = signal('');
  protected readonly sending = signal(false);
  protected readonly started = signal(false);

  private readonly messageList = viewChild.required<ElementRef<HTMLElement>>('messageList');
  private readonly input = viewChild.required<ElementRef<HTMLInputElement>>('input');

  constructor() {
    // After each render: keep the newest message in view, and put the cursor
    // back in the input once a reply has arrived
    afterRenderEffect(() => {
      this.messages();
      const list = this.messageList().nativeElement;
      list.scrollTop = list.scrollHeight;
      if (!this.sending()) {
        this.input().nativeElement.focus();
      }
    });
  }

  protected submit(event: Event): void {
    event.preventDefault();
    this.send(this.draft());
  }

  protected send(text: string): void {
    const message = text.trim();
    if (!message || this.sending()) {
      return;
    }
    this.started.set(true);
    this.draft.set('');
    // Clear the box directly too: if no render ran since the last keystroke, the
    // [value] binding still holds '' and would not see a change
    this.input().nativeElement.value = '';
    this.sending.set(true);
    this.addMessage({ sender: 'user', text: message });

    this.chatService
      .send(message)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (reply) => this.finish({ sender: 'bot', text: reply }),
        error: (error: Error) => this.finish({ sender: 'bot', text: error.message, error: true }),
      });
  }

  private finish(reply: ChatMessage): void {
    this.addMessage(reply);
    this.sending.set(false);
  }

  private addMessage(message: ChatMessage): void {
    this.messages.update((messages) => [...messages, message]);
  }
}
