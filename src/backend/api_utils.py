import requests
import json
import base64
from icecream import ic

# Get token from Spotify API returns the access token
def get_token() -> str:
    with open("api_creds.json") as f:
        creds = json.load(f)
    client_id = creds["client_id"]
    client_secret = creds["client_secret"]
    auth = f"{client_id}:{client_secret}"
    b64_auth = base64.b64encode(auth.encode()).decode()
    auth_url = "https://accounts.spotify.com/api/token"
    auth_headers = {
        'Authorization': f"Basic {b64_auth}"
    }
    auth_data = {
        'grant_type': 'client_credentials'
    }
    auth_response = requests.post(auth_url, headers=auth_headers, data=auth_data)
    return auth_response.json()["access_token"]

# Get songs from Spotify API
# Will take params later for genre, artist, existing playlist, etc.
# For now, gets list of random songs
def find_songs(token: str):
    url = "https://api.spotify.com/v1/search"
    headers = {
        'Authorization': f"Bearer {token}"
    }
    params = {
        "query": "kanye",
        "limit": 50,
        "type": "track",
    }
    response = requests.get(url, headers=headers, params=params)
    songs = []
    for song in response.json()["tracks"]["items"]:
        songs.append((f"{song['name']} by {song['artists'][0]['name']}", song["duration_ms"], song["uri"]))
    return songs

# Match songs to intervals
# Returns the "URIs" string that the add to playlist API needs
def match_songs(songs: list, intervals: list[int]) -> str:
    uris = []
    for interval in intervals:
        min_song = songs[0]
        for song in songs:
            _, min_duration, _ = min_song
            _, duration, _ = song
            if abs(duration - interval) < abs(min_duration - interval):
                min_song = song
        uris.append(min_song[2])
        print(f"Matched {min_song[0]} of length {round(min_song[1] / 60000, 2)} to interval of {round(interval / 60000, 2)} minutes")
        songs.remove(min_song)
        if songs == []:
            break
    return ",".join(uris)


# Makes array with the intervals in ms time from minutes
def fill_workout(int_length: int, rest_length: int, total_length: int) -> list[int]:
    intervals = []
    rest_period = True
    while (sum(intervals) / 60000) < total_length:
        if rest_period:
            intervals.append(rest_length * 60000)
        elif not rest_period:
            intervals.append(int_length * 60000)
        rest_period = not rest_period
    intervals.append((total_length - (sum(intervals) / 60000)) * 60000) # fill the time with a final interval
    return intervals

def main():
    int_length = int(input("Enter the length of your active interval: ")) # in minutes
    rest_length = int(input("Enter the length of your rest interval: ")) # in minutes
    total_length = int(input("Enter the total length of your workout: ")) # in minutes
    workout_times = fill_workout(int_length, rest_length, total_length)
    token = get_token()
    songs = find_songs(token)
    uris = match_songs(songs, workout_times)
    print(uris)


if __name__ == "__main__":
    main()
