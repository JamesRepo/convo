import { Component, Input, OnChanges, SimpleChanges, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { ChatMessage, MessageType } from '../../../core/models/message.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-message-list',
    standalone: true,
    imports: [CommonModule, MatCardModule],
    templateUrl: './message-list.component.html',
    styleUrls: ['./message-list.component.scss']
})
export class MessageListComponent implements OnChanges, AfterViewChecked {
    @Input() messages: ChatMessage[] = [];
    @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

    private shouldScrollToBottom = true;
    currentUsername: string | null;

    MessageType = MessageType;

    constructor(private authService: AuthService) {
        this.currentUsername = this.authService.getCurrentUsername();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['messages']) {
            this.shouldScrollToBottom = true;
        }
    }

    ngAfterViewChecked(): void {
        if (this.shouldScrollToBottom) {
            this.scrollToBottom();
            this.shouldScrollToBottom = false;
        }
    }

    isOwnMessage(message: ChatMessage): boolean {
        return message.senderUsername === this.currentUsername;
    }

    private scrollToBottom(): void {
        try {
            if (this.scrollContainer) {
                this.scrollContainer.nativeElement.scrollTop =
                    this.scrollContainer.nativeElement.scrollHeight;
            }
        } catch(err) {
            console.error('Error scrolling to bottom:', err);
        }
    }
}