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

    weatherInfo: WeatherInfo = null;

    ngOnInit(): void {
        this.customTitle = "accu";
        let apiUrl = 'http://localhost:8080/' + this.customTitle;
        let test = this.http.get(apiUrl)
        .subscribe((data: WeatherInfo)=>{
            this.weatherInfo = data;
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        let apiUrl = 'http://localhost:8080/' + this.customTitle;
                let test = this.http.get(apiUrl)
                .subscribe((data: WeatherInfo)=>{
                    this.weatherInfo = data;
                });
      }

  getUrl(provider: string): string {
    switch (provider) {
      case 'accu': {
        return 'https://www.accuweather.com/';
        break;
      }
      case 'w2u': {
        return 'https://www.weather2umbrella.com/';
        break;
      }
      case 'rhmz': {
        return 'http://www.hidmet.gov.rs/';
        break;
      }
    }
  }
}
