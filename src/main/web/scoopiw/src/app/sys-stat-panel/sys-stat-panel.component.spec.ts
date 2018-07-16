/* tslint:disable:no-unused-variable */
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { SysStatPanelComponent } from './sys-stat-panel.component';

describe('SysStatPanelComponent', () => {
  let component: SysStatPanelComponent;
  let fixture: ComponentFixture<SysStatPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SysStatPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SysStatPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
