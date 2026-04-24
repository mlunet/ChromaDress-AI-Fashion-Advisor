import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { passwordMatchValidator } from '../../../../core/validation/password.validator';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  errorMessage: string | null = null;

  registerForm = this.fb.group(
    {
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator },
  );

  onSubmit() {
    if (this.registerForm.valid) {
      const { username, password, email } = this.registerForm.getRawValue();

      this.authService.register({ username, password, email } as any).subscribe({
        next: () => {
          console.log('Registered and logged in successfully.');
          this.router.navigate(['/wardrobe']);
        },
        error: (err) => {
          this.errorMessage = 'Error during registration.';
          console.error('err');
        },
      });
    }
  }
}
