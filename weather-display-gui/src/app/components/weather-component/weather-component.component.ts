import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'weather-component',
  templateUrl: './weather-component.component.html',
  styleUrls: ['./weather-component.component.css']
})
export class WeatherComponent implements OnInit {

    @Input()
    customTitle: string;

  constructor() { }

  ngOnInit(): void {
    this.customTitle = "test 1";
  }

}
