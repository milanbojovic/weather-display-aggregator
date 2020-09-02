import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CityList} from "../../model/city-list.model";


@Component({
  selector: 'weather-component',
  templateUrl: './weather-component.component.html',
  styleUrls: ['./weather-component.component.css']
})
export class WeatherComponent implements OnInit {

    constructor(private http: HttpClient) { }

    @Input()
    customTitle: string;

    cityList: CityList = null;

    ngOnInit(): void {
        this.customTitle = 'accu';
        const apiUrl = 'http://localhost:8080/' + this.customTitle;
        const test = this.http.get(apiUrl)
        .subscribe((data: CityList) => {
            this.cityList = data;
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        const apiUrl = 'http://localhost:8080/' + this.customTitle;
        const test = this.http.get(apiUrl)
                .subscribe((data: CityList) => {
                    this.cityList = data;
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
