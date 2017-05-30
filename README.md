# KeepMoving
Android application that uses accelerometer sensor data and Google location to get user's speed. 

Before running the application or reading the code, please consider the following points:

1. If the App has no Read data permission, one window will be prompted asking for the user's permission;
2. If the App has no Location permission, one window will be prompted asking for the user's permission;
3. The user must give both permission to the application;
4. The user must choose one music for running and one music for biking. The screen has two buttons, one for choosing the running song and the other one to the biking song;
5. Thresholds speed: Walking/Running > 1 and <= 10 km/h Biking > 10 and <= 20 km/h User inside a bus or a car > 20 km/h
6. Thresholds Accelerometer: User is moving: Acceleration > 1.1 or < 0.9
In order to play a song, the user must be moving (acceleration detection) and with speed > 1 and <= 20 km/h.
7. All the references to other pages or repositories are included in the code.
