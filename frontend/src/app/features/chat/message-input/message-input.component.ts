import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-message-input',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule
    ],
    templateUrl: './message-input.component.html',
    styleUrls: ['./message-input.component.scss']
})
export class MessageInputComponent {
    @Output() messageSent = new EventEmitter<string>();
    @Output() typing = new EventEmitter<void>();

    messageText = '';
    private typingTimeout: any;

    onTyping(): void {
        // Clear previous timeout
        if (this.typingTimeout) {
            clearTimeout(this.typingTimeout);
        }

        // Emit typing event
        this.typing.emit();

        // Set timeout to stop showing typing indicator
        this.typingTimeout = setTimeout(() => {
            // Typing stopped
        }, 3000);
    }

    sendMessage(): void {
        const trimmedMessage = this.messageText.trim();

        if (trimmedMessage) {
            this.messageSent.emit(trimmedMessage);
            this.messageText = '';
        }
    }

    onKeyPress(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }
}