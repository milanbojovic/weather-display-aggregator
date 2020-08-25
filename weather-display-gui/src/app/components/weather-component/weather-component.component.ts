import { Component, OnInit, Input, SimpleChanges } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { WeatherInfo } from "../../model/weather-info.model";

@Component({
  selector: 'weather-component',
  templateUrl: './weather-component.component.html',
  styleUrls: ['./weather-component.component.css']
})
export class WeatherComponent implements OnInit {

    constructor(private http: HttpClient) { }

    @Input()
    customTitle: string;

    weatherInfo: WeatherInfo;

    ngOnInit(): void {
        this.customTitle = "aque";
        let apiUrl = 'http://localhost:8080/rhmdz';
        let test = this.http.get(apiUrl)
        .subscribe((data: WeatherInfo)=>{
            this.weatherInfo = data;
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        let apiUrl = './assets/data/weather/' + this.customTitle + '.json';
                let test = this.http.get(apiUrl)
                .subscribe((data: WeatherInfo)=>{
                    this.weatherInfo = data;
                });
      }
}