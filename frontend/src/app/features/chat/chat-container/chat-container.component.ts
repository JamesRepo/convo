import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { Subject, takeUntil } from 'rxjs';
import { ChatRoomListComponent } from '../chat-room-list/chat-room-list.component';
import { ChatWindowComponent } from '../chat-window/chat-window.component';
import { ChatRoom } from '../../../core/models/chat-room.model';
import { ChatService } from '../../../core/services/chat.service';
import { AuthService } from '../../../core/services/auth.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { UserService } from '../../../core/services/user.service';

@Component({
    selector: 'app-chat-container',
    standalone: true,
    imports: [
        CommonModule,
        MatSidenavModule,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        ChatRoomListComponent,
        ChatWindowComponent
    ],
    templateUrl: './chat-container.component.html',
    styleUrls: ['./chat-container.component.scss']
})
export class ChatContainerComponent implements OnInit, OnDestroy {
    currentUsername: string | null;
    private destroy$ = new Subject<void>();

    constructor(
        private chatService: ChatService,
        private authService: AuthService,
        private webSocketService: WebSocketService,
        private userService: UserService
    ) {
        this.currentUsername = this.authService.getCurrentUsername();
    }

    ngOnInit(): void {
        // Update user status to online
        this.userService.updateStatus('ONLINE').subscribe();

        // Connect to WebSocket
        this.webSocketService.connect();

        // Monitor connection status
        this.webSocketService.connected$
            .pipe(takeUntil(this.destroy$))
            .subscribe(connected => {
                if (!connected) {
                    console.log('WebSocket disconnected, attempting to reconnect...');
                }
            });
    }

    ngOnDestroy(): void {
        // Update user status to offline
        this.userService.updateStatus('OFFLINE').subscribe();

        // Disconnect WebSocket
        this.webSocketService.disconnect();

        this.destroy$.next();
        this.destroy$.complete();
    }

    onRoomSelected(room: ChatRoom | null): void {
        this.chatService.setCurrentRoom(room);
    }

    logout(): void {
        this.authService.logout();
    }
}