import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ChatRoom } from '../../../core/models/chat-room.model';

export interface RoomDialogData {
    room?: ChatRoom;
}

@Component({
    selector: 'app-room-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule
    ],
    templateUrl: './room-dialog.component.html',
    styleUrls: ['./room-dialog.component.scss']
})
export class RoomDialogComponent implements OnInit {
    roomForm: FormGroup;
    isEditMode: boolean;

    constructor(
        private fb: FormBuilder,
        private dialogRef: MatDialogRef<RoomDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: RoomDialogData
    ) {
        this.isEditMode = !!data.room;
        this.roomForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
            description: ['', [Validators.maxLength(500)]]
        });
    }

    ngOnInit(): void {
        if (this.isEditMode && this.data.room) {
            this.roomForm.patchValue({
                name: this.data.room.name,
                description: this.data.room.description || ''
            });
        }
    }

    onCancel(): void {
        this.dialogRef.close();
    }

    onSubmit(): void {
        if (this.roomForm.valid) {
            this.dialogRef.close(this.roomForm.value);
        }
    }

    get title(): string {
        return this.isEditMode ? 'Edit Chat Room' : 'Create Chat Room';
    }

    get submitButtonText(): string {
        return this.isEditMode ? 'Update' : 'Create';
    }
}

