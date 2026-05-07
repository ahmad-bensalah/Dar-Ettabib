import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  userType: 'PATIENT' | 'DOCTOR' = 'PATIENT';
  
  // Common
  username = '';
  password = '';
  nom = '';
  
  // Doctor
  email = '';
  specialite = '';
  
  // Patient
  dateNaissance = '';
  malade = false;
  adresse = '';
  codePostal = '';
  numeroTelephone = '';
  titre = 'MR';
  
  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSignup(): void {
    if (!this.username || !this.password || !this.nom) {
      this.errorMessage = 'Please fill out all required fields';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload: any = {
      username: this.username,
      password: this.password,
      nom: this.nom
    };

    let obs;

    if (this.userType === 'PATIENT') {
      payload.dateNaissance = this.dateNaissance || null;
      payload.malade = this.malade;
      payload.adresse = this.adresse;
      payload.codePostal = this.codePostal;
      payload.numeroTelephone = this.numeroTelephone;
      // You must handle titre format based on backend expectation
      obs = this.authService.signupPatient(payload);
    } else {
      payload.email = this.email;
      payload.specialite = this.specialite;
      obs = this.authService.signupDoctor(payload);
    }

    obs.subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Registration successful! You can now log in.';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.error || 'Registration failed. Please try again.';
      }
    });
  }
}
