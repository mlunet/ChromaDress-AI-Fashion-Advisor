import { Component, inject, output } from '@angular/core';
import { ClothingService } from '../../services/clothing.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-clothing-upload',
  imports: [ReactiveFormsModule],
  templateUrl: './clothing-upload.component.html',
  styleUrl: './clothing-upload.component.css',
})
export class ClothingUploadComponent {
  private fb = inject(FormBuilder);
  private clothingService = inject(ClothingService);

  uploadStarted = output<void>();
  uploadFinished = output<void>();

  selectedFile: File | null = null;

  uploadForm = this.fb.group({
    name: ['', Validators.required],
    category: ['', Validators.required],
  });

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onUpload() {
    if (this.uploadForm.valid && this.selectedFile) {
      this.uploadStarted.emit();
      const { name, category } = this.uploadForm.getRawValue();

      this.clothingService.uploadItem(name!, category!, this.selectedFile).subscribe({
        next: () => {
          this.uploadFinished.emit();
          this.uploadForm.reset();
          this.selectedFile = null;
        },
        error: (err) => {
          console.error($localize`:@@upload.errorLog:Error during upload.`, err);
          this.uploadFinished.emit();
        },
      });
    }
  }
}
