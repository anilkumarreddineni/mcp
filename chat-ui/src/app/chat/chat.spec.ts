import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of, Subject, throwError } from 'rxjs';

import { Chat } from './chat';
import { ChatService } from './chat.service';

describe('Chat', () => {
  let fixture: ComponentFixture<Chat>;
  let element: HTMLElement;
  let send: jasmine.Spy<(message: string) => Observable<string>>;

  beforeEach(async () => {
    send = jasmine.createSpy('send');
    await TestBed.configureTestingModule({
      imports: [Chat],
      providers: [provideZonelessChangeDetection(), { provide: ChatService, useValue: { send } }],
    }).compileComponents();

    fixture = TestBed.createComponent(Chat);
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  const messages = () => Array.from(element.querySelectorAll('.message')).map((m) => m.textContent?.trim());

  async function type(text: string) {
    const input = element.querySelector('input')!;
    input.value = text;
    input.dispatchEvent(new Event('input'));
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  it('shows the greeting and suggestions', () => {
    expect(messages()).toEqual(['Hi! I can look up orders and their status. What would you like to know?']);
    expect(element.querySelectorAll('.suggestions button').length).toBe(3);
  });

  it('sends a typed message and shows the reply', async () => {
    send.and.returnValue(of('Order 1003 has shipped.'));

    await type('  Status of 1003?  ');

    expect(send).toHaveBeenCalledOnceWith('Status of 1003?');
    expect(messages().slice(1)).toEqual(['Status of 1003?', 'Order 1003 has shipped.']);
    expect(element.querySelector('.suggestions')).toBeNull();
    expect(element.querySelector('input')!.value).toBe('');
  });

  it('shows "Thinking..." and disables input while waiting', async () => {
    const reply = new Subject<string>();
    send.and.returnValue(reply);

    await type('Show me all orders');

    expect(element.querySelector('.typing')?.textContent).toContain('Thinking...');
    expect(element.querySelector('input')!.disabled).toBeTrue();

    reply.next('Here are your orders.');
    reply.complete();
    await fixture.whenStable();

    expect(element.querySelector('.typing')).toBeNull();
    expect(element.querySelector('input')!.disabled).toBeFalse();
  });

  it('sends a suggestion when clicked', async () => {
    send.and.returnValue(of('Order 1003 has shipped.'));

    (element.querySelector('.suggestions button') as HTMLButtonElement).click();
    await fixture.whenStable();

    expect(send).toHaveBeenCalledOnceWith("What's the status of order 1003?");
  });

  it('shows errors as error messages', async () => {
    send.and.returnValue(throwError(() => new Error("Can't reach the chat service.")));

    await type('hi');

    const error = element.querySelector('.message.error');
    expect(error?.textContent).toContain("Can't reach the chat service.");
  });

  it('ignores blank messages', async () => {
    await type('   ');

    expect(send).not.toHaveBeenCalled();
  });
});
