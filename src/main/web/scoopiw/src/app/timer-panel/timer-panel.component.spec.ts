/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { TimerPanelComponent } from './timer-panel.component';

describe('TimerPanelComponent', () => {
  let component: TimerPanelComponent;
  let fixture: ComponentFixture<TimerPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TimerPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TimerPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
