import datetime
import requests

from geopy.geocoders import Nominatim


def check_dates(date_start, date_end):
    return date_start <= date_end # and date_start >= datetime.date.today()


def get_location(city):
    geolocator = Nominatim(user_agent='REST_univeristy_excercise')
    return geolocator.geocode(city)


def get_openweather_data(app, city, date_start, date_end):
    api_key = app.config['API_KEY']

    location = get_location(city)

    parameters = {
        'lat': location.latitude,
        'lon': location.longitude,
        'exclude': 'current,minutely,hourly,alerts',
        'appid': api_key,
        'units': 'metric'
    }

    response = requests.get('https://api.openweathermap.org/data/2.5/onecall', params=parameters)

    data = response.json()['daily']
    result = []

    for day in data:
        date = datetime.date.fromtimestamp(day['dt'])
        if date >= date_start and date <= date_end:
            
            # calculate avg temp
            avg_temp = []
            
            for key, value in day['temp'].items():
                if not key == 'min' and not key == 'max':
                    avg_temp.append(value)

            avg_temp = sum(avg_temp) / len(avg_temp)

            # get min and max temperatures
            min_temp = day['temp']['min']
            max_temp = day['temp']['max']

            # get average 'feels like' temperature
            feels_like = sum(day['feels_like'].values()) / len(day['feels_like'].values())
            
            # get overall weather info
            precipation_id = day['weather'][0]['id']
            precipation = day['weather'][0]['main']

            date = date.strftime('%Y/%m/%d')

            result.append(
                {
                    'date': date,
                    'avg_temp': round(avg_temp, 1),
                    'min_temp': round(min_temp, 1),
                    'max_temp': round(max_temp, 1),
                    'feels_like': round(feels_like, 1),
                    'precipation_id': precipation_id,
                    'precipation': precipation
                }
            )

    return result


def get_metaweather_data(city, date_start, date_end):

    def daterange(start_date, end_date):
        for n in range(int((end_date - start_date).days) + 1):
            yield start_date + datetime.timedelta(n)
    
    location = get_location(city)

    parameters = {
        'lattlong': f'{location.latitude},{location.longitude}'
    }
    
    response = requests.get('https://www.metaweather.com/api/location/search/', params=parameters)
    woeid = response.json()[0]['woeid']

    result = []

    for date in daterange(date_start, date_end):
        date = date.strftime('%Y/%m/%d')
        response = requests.get(f'https://www.metaweather.com/api/location/{woeid}/{date}')
        data = response.json()

        min_temp = []
        max_temp = []
        avg_temp = []
        predictability = []
        
        for forecast in data:
            min_temp.append(forecast['min_temp'])
            max_temp.append(forecast['max_temp'])
            avg_temp.append(forecast['the_temp'])
            predictability.append(forecast['predictability'])

        min_temp = sum(min_temp) / len(min_temp)
        max_temp = sum(max_temp) / len(max_temp)
        avg_temp = sum(avg_temp) / len(avg_temp)
        predictability = sum(predictability) / len(predictability)

        result.append(
            {
                'date': date,
                'min_temp': round(min_temp, 1),
                'max_temp': round(max_temp, 1),
                'avg_temp': round(avg_temp, 1),
                'predictability': round(predictability, 1)
            }
        )

    return result
