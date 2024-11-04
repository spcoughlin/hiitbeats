// src/App.js
import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
  const [userId, setUserId] = useState(null);

  const handleLogin = async () => {
    try {
      // Fetch the login link from your REST API
      const response = await fetch('http://localhost:9000/login-link');
      const data = await response.json();
      const { loginLink } = data; // Extract the loginLink from the response
      window.location.href = loginLink;
    } catch (error) {
      console.error('Error fetching the login link:', error);
    }
  };

  useEffect(() => {
    // Check if the URL contains an authorization code after redirect from Spotify
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    if (code) {
      // Send the code to the backend to exchange for access token and get userId
      const fetchUserId = async () => {
        try {
          const response = await fetch('http://localhost:9000/exchange-get-user', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ authCode: code }),
          });
          const data = await response.json();
          const { userId } = data;
          setUserId(userId);

          // Clean up the URL by removing the query parameters
          window.history.replaceState({}, document.title, window.location.pathname);
        } catch (error) {
          console.error('Error fetching userId:', error);
        }
      };
      fetchUserId();
    }
  }, []);

  return (
    <div className="App">
      {userId ? (
        <p className="welcome-text">Logged in as {userId}</p>
      ) : (
        <button onClick={handleLogin} className="login-button">
          Log in with Spotify
        </button>
      )}
    </div>
  );
}

export default App;

