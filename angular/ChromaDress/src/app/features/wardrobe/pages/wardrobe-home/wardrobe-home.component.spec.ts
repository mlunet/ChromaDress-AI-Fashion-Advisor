import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WardrobeHomeComponent } from './wardrobe-home.component';

describe('WardrobeHomeComponent', () => {
  let component: WardrobeHomeComponent;
  let fixture: ComponentFixture<WardrobeHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WardrobeHomeComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(WardrobeHomeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
