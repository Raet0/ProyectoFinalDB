import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Episodios } from './episodios';

describe('Episodios', () => {
  let component: Episodios;
  let fixture: ComponentFixture<Episodios>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Episodios]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Episodios);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
