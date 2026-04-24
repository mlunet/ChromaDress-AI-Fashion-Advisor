import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClothingUploadComponent } from './clothing-upload.component';

describe('ClothingUploadComponent', () => {
  let component: ClothingUploadComponent;
  let fixture: ComponentFixture<ClothingUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClothingUploadComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ClothingUploadComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
