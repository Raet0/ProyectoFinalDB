import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Locutores } from './locutores';

describe('Locutores', () => {
  let component: Locutores;
  let fixture: ComponentFixture<Locutores>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Locutores]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Locutores);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
