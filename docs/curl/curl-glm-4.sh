curl -X POST \
        -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiMWM0ZTEwY2Q1ZjVhYTdkYjhmNzczZGU4ZDhlNzM2Y2IiLCJleHAiOjE3MjMyOTM2MTM1MTgsInRpbWVzdGFtcCI6MTcyMzI5MTgxMzUyN30.OrSzJso17qcfUOiniEd-e7qRWIfqX7tRWw-_vSW1i78" \
        -H "Content-Type: application/json" \
        -H "User-Agent: Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)" \
        -d '{
          "model":"glm-4",
          "stream": "true",
          "messages": [
              {
                  "role": "user",
                  "content": "1+1"
              }
          ]
        }' \
  https://open.bigmodel.cn/api/paas/v4/chat/completions
