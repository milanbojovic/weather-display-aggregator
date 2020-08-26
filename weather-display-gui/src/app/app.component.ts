import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'weather-display-gui';
  accuWeather = 'accuw';

    passTheSalt(id){
            this.accuWeather = id;
            console.log(id);
       }
}
