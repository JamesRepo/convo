import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ChatService } from '../../../core/services/chat.service';
import { ChatRoom } from '../../../core/models/chat-room.model';
import { RoomDialogComponent, RoomDialogData } from '../room-dialog/room-dialog.component';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-chat-room-list',
    standalone: true,
    imports: [
        CommonModule,
        MatListModule,
        MatIconModule,
        MatButtonModule,
        MatDialogModule,
        MatSnackBarModule
    ],
    templateUrl: './chat-room-list.component.html',
    styleUrls: ['./chat-room-list.component.scss']
})
export class ChatRoomListComponent implements OnInit {
    @Output() roomSelected = new EventEmitter<ChatRoom | null>();

    chatRooms$: Observable<ChatRoom[]>;
    selectedRoom: ChatRoom | null = null;

    constructor(
        private chatService: ChatService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar
    ) {
        this.chatRooms$ = this.chatService.chatRooms$;
    }

    ngOnInit(): void {
        this.chatService.loadChatRooms().subscribe();
    }

    selectRoom(room: ChatRoom): void {
        this.selectedRoom = room;
        this.roomSelected.emit(room);
    }

    createRoom(): void {
        const dialogRef = this.dialog.open(RoomDialogComponent, {
            width: '500px',
            data: {} as RoomDialogData
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.chatService.createChatRoom(result.name, result.description).subscribe({
                    next: () => {
                        this.snackBar.open('Chat room created successfully', 'Close', {
                            duration: 3000
                        });
                        this.chatService.loadChatRooms().subscribe();
                    },
                    error: (error) => {
                        this.snackBar.open('Failed to create chat room', 'Close', {
                            duration: 3000
                        });
                        console.error('Error creating chat room:', error);
                    }
                });
            }
        });
    }

    editRoom(room: ChatRoom, event: Event): void {
        event.stopPropagation(); // Prevent room selection when clicking edit

        const dialogRef = this.dialog.open(RoomDialogComponent, {
            width: '500px',
            data: { room } as RoomDialogData
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.chatService.updateChatRoom(room.id, result.name, result.description).subscribe({
                    next: () => {
                        this.snackBar.open('Chat room updated successfully', 'Close', {
                            duration: 3000
                        });
                        this.chatService.loadChatRooms().subscribe();
                    },
                    error: (error) => {
                        this.snackBar.open('Failed to update chat room', 'Close', {
                            duration: 3000
                        });
                        console.error('Error updating chat room:', error);
                    }
                });
            }
        });
    }

    deleteRoom(room: ChatRoom, event: Event): void {
        event.stopPropagation(); // Prevent room selection when clicking delete

        const dialogRef = this.dialog.open(ConfirmDialogComponent, {
            width: '400px',
            data: {
                title: 'Delete Chat Room',
                message: `Are you sure you want to delete "${room.name}"? This action cannot be undone.`,
                confirmText: 'Delete',
                cancelText: 'Cancel'
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.chatService.deleteChatRoom(room.id).subscribe({
                    next: () => {
                        this.snackBar.open('Chat room deleted successfully', 'Close', {
                            duration: 3000
                        });
                        this.chatService.loadChatRooms().subscribe();
                        // Clear selection if deleted room was selected
                        if (this.selectedRoom?.id === room.id) {
                            this.selectedRoom = null;
                            this.roomSelected.emit(null);
                        }
                    },
                    error: (error) => {
                        this.snackBar.open('Failed to delete chat room', 'Close', {
                            duration: 3000
                        });
                        console.error('Error deleting chat room:', error);
                    }
                });
            }
        });
    }
}