import { TestBed } from '@angular/core/testing';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { WebSocketService } from './websocket.service';
import { AuthService } from './auth.service';

class MockSubscription implements StompSubscription {
    id: string;
    private unsubscribeSpy: jasmine.Spy;

    constructor(destination: string) {
        this.id = destination;
        this.unsubscribeSpy = jasmine.createSpy(`unsubscribe-${destination}`);
    }

    unsubscribe(): void {
        this.unsubscribeSpy();
    }

    get spy(): jasmine.Spy {
        return this.unsubscribeSpy;
    }
}

describe('WebSocketService', () => {
    let service: WebSocketService;
    let authService: jasmine.SpyObj<AuthService>;
    let mockClient: Client;
    const subscriptionRegistry = new Map<string, MockSubscription>();

    beforeEach(() => {
        subscriptionRegistry.clear();
        authService = jasmine.createSpyObj('AuthService', ['getToken', 'getCurrentUsername']);
        authService.getToken.and.returnValue('token');
        authService.getCurrentUsername.and.returnValue('tester');

        TestBed.configureTestingModule({
            providers: [
                WebSocketService,
                { provide: AuthService, useValue: authService }
            ]
        });

        service = TestBed.inject(WebSocketService);

        mockClient = {
            connected: true,
            subscribe: jasmine.createSpy('subscribe').and.callFake((destination: string, callback: (message: IMessage) => void) => {
                const subscription = new MockSubscription(destination);
                subscriptionRegistry.set(destination, subscription);
                return subscription as StompSubscription;
            }),
            publish: jasmine.createSpy('publish'),
            deactivate: jasmine.createSpy('deactivate'),
            activate: jasmine.createSpy('activate'),
        } as unknown as Client;

        (service as any).stompClient = mockClient;
    });

    it('should unsubscribe from previous room before subscribing to a new room', () => {
        const unsubscribeSpy = spyOn(service, 'unsubscribeFromRoom').and.callThrough();

        service.subscribeToRoom(1);
        expect((service as any).currentRoomId).toBe(1);

        const firstRoomMessageSub = subscriptionRegistry.get('/topic/room/1');
        const firstRoomTypingSub = subscriptionRegistry.get('/topic/typing/1');
        expect(firstRoomMessageSub).toBeDefined();
        expect(firstRoomTypingSub).toBeDefined();

        service.subscribeToRoom(2);

        expect(unsubscribeSpy).toHaveBeenCalledWith(1);
        expect(firstRoomMessageSub?.spy).toHaveBeenCalled();
        expect(firstRoomTypingSub?.spy).toHaveBeenCalled();
        expect((service as any).currentRoomId).toBe(2);
    });

    it('should only send leave messages when a subscription existed', () => {
        const leaveSpy = spyOn<any>(service as any, 'sendLeaveMessage');

        service.unsubscribeFromRoom(999);
        expect(leaveSpy).not.toHaveBeenCalled();

        service.subscribeToRoom(3);
        service.unsubscribeFromRoom(3);
        expect(leaveSpy).toHaveBeenCalledWith(3);
    });

    it('should reset currentRoomId when disconnecting', () => {
        const unsubscribeSpy = spyOn(service, 'unsubscribeFromRoom').and.callThrough();

        service.subscribeToRoom(4);
        service.disconnect();

        expect(unsubscribeSpy).toHaveBeenCalledWith(4);
        expect((service as any).currentRoomId).toBeNull();
    });
});
