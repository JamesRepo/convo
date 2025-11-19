export interface ChatMessage {
    id?: number;
    senderUsername: string;
    senderId: number;
    senderAvatar?: string;
    chatRoomId: number;
    content: string;
    type: MessageType;
    timestamp: Date;
    edited: boolean;
    readByCount: number;
}

export enum MessageType {
    CHAT = 'CHAT',
    JOIN = 'JOIN',
    LEAVE = 'LEAVE',
    TYPING = 'TYPING',
    STOP_TYPING = 'STOP_TYPING',
    ORACLE = 'ORACLE',
    SYSTEM = 'SYSTEM'
}