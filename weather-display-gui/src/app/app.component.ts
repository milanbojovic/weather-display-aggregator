import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'weather-display-gui';
  weatherDisplayTitle = 'accu';

    passTheSalt(id){
            this.weatherDisplayTitle = id;
            console.log(id);
       }
}
