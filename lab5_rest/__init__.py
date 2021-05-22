import os

from flask import Flask, render_template
from flask.globals import request
from flask.helpers import flash, url_for
from werkzeug.utils import redirect

from .utils import *


def create_app(test_config=None):
    app = Flask(__name__, instance_relative_config=True)
    
    if not test_config:
        app.config.from_pyfile('config.py', silent=False)
    else:
        app.config.from_mapping(test_config)
    
    try:
        os.makedirs(app.instance_path)
    except OSError:
        pass

    @app.route('/do_i_need_a_jacket', methods=('GET', 'POST'))
    def dinaj():
        if request.method == 'POST':
            city = request.form['city']
            date_start = datetime.datetime.strptime(request.form['date_start'], '%Y-%m-%d').date()
            date_end = datetime.datetime.strptime(request.form['date_end'], '%Y-%m-%d').date()

            if not check_dates(date_start, date_end):
                flash('Incorrect date, please choose a date in the future and assure that the second date is greater than the first.', category='error')
                return render_template('form.html')

            openweather_data = get_openweather_data(app, city, date_start, date_end)
            metaweather_data = get_metaweather_data(city, date_start, date_end)

            return render_template('results.html' , city=city, date_start=date_start, date_end=date_end, openweather=openweather_data, metaweather=metaweather_data)

        return render_template('form.html')

    return app