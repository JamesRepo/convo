import { User } from './user.model';
import { ChatMessage } from './message.model';

export interface ChatRoom {
    id: number;
    name: string;
    description?: string;
    type: 'PUBLIC' | 'PRIVATE' | 'DIRECT_MESSAGE';
    createdBy: User;
    createdAt: Date;
    memberCount: number;
    lastMessage?: ChatMessage;
}