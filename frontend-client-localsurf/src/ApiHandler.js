import React, { useState } from 'react';
import endpoints from "./endpoints";

function ApiHandler() {
  const [postData, setPostData] = useState('');
  const [keyword, setKeyword] = useState('');
  const [getResponse, setGetResponse] = useState('');

  const handlePostClick = async () => {
    const response = await fetch(endpoints.apiGW, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: postData,
    });
    if(response.status === 200) {
      setPostData('');
    }

    const data = await response.json();
    console.log('Post Response:', data);
  };

  const handleGetClick = async () => {
    const response = await fetch(`${endpoints.apiGW}?author=${keyword}`);
    const data = await response.json();
    setGetResponse(data);
  };

  return (
      <div>
        <div>
          <h2>Post Quote</h2>

          <textarea
              rows="10"
              cols="50"
              value={postData}
              onChange={(e) => setPostData(e.target.value)}
              placeholder="Enter new Quote..."
          />
          <br/>
          <br/>

          <button onClick={handlePostClick}>Post</button>
        </div>

        <div>
          <h2>Get Quote</h2>
          <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder={"Author"}
          />
          <br/>
          <br/>

          <button onClick={handleGetClick}>Get</button>
          <pre>{JSON.stringify(getResponse, null, 2)}</pre>
        </div>
      </div>
  );
}

export default ApiHandler;
