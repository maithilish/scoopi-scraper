/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { MeterPanelComponent } from './meter-panel.component';

describe('MeterPanelComponent', () => {
  let component: MeterPanelComponent;
  let fixture: ComponentFixture<MeterPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MeterPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MeterPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
