import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/services/auth.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        MatSnackBarModule,
        MatIconModule
    ],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent {
    loginForm: FormGroup;
    loading = false;
    hidePassword = true;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private snackBar: MatSnackBar
    ) {
        this.loginForm = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3)]],
            password: ['', [Validators.required, Validators.minLength(6)]]
        });
    }

    onSubmit(): void {
        if (this.loginForm.valid && !this.loading) {
            this.loading = true;

            this.authService.login(this.loginForm.value).subscribe({
                next: () => {
                    this.snackBar.open('Login successful!', 'Close', { duration: 3000 });
                    this.loginForm.reset();
                    this.router.navigate(['/chat']);
                },
                error: (error) => {
                    this.loading = false;
                    let message = 'Login failed. Please try again.';

                    if (error.error?.message) {
                        message = error.error.message;
                    } else if (error.status === 401) {
                        message = 'Invalid username or password.';
                    } else if (error.status === 0) {
                        message = 'Unable to connect to server. Please check your connection.';
                    }

                    this.snackBar.open(message, 'Close', { duration: 5000 });
                }
            });
        }
    }
}