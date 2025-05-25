# -*- coding: utf-8 -*-

import requests
from concurrent.futures import ThreadPoolExecutor, as_completed

# URL и JWT
URL_LIST = ["http://localhost:8080/clk/shorten", "http://localhost:8082/clk/shorten", "http://localhost:8084/clk/shorten"]
#URL = "http://localhost:8080/clk/shorten"
JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzQ4MjExOTE3LCJleHAiOjE3NDgyMTU1MTd9.Um4ZETQCMHbJ8Xo7j0I5bw48lSY03NxewpoZLeSbl1g"
HEADERS = {
    "Authorization": f"Bearer {JWT}",
    "Content-Type": "application/json"
}
DATA = {"fullUrl": "https://example.com"}

def single_request(i):
    resp = requests.post(URL_LIST[i % 3], json=DATA, headers=HEADERS)
    return i, resp.status_code

def main():
    total_requests = 120
    successes = 0
    fails = 0

    with ThreadPoolExecutor(max_workers=total_requests) as executor:
        futures = [executor.submit(single_request, i) for i in range(total_requests)]
        for future in as_completed(futures):
            i, code = future.result()
            if code in (200, 201):
                print(f"Request {i}: OK ({code})")
                successes += 1
            else:
                print(f"Request {i}: FAIL ({code})")
                fails += 1

    print("=================================")
    print(f"Успешных (200): {successes}")
    print(f"Неуспешных (429 и прочие): {fails}")

if __name__ == "__main__":
    main()
