import requests
import sys

BASE_URL = "http://localhost:8080"

def log(msg, status="INFO"):
    print(f"[{status}] {msg}")

def check_endpoint(method, path, expected_status=200):
    url = f"{BASE_URL}{path}"
    try:
        if method == "GET":
            response = requests.get(url)
        elif method == "POST":
            response = requests.post(url)
        elif method == "PUT":
            response = requests.put(url)
        elif method == "PATCH":
            response = requests.patch(url)
        else:
            log(f"Unsupported method {method}", "ERROR")
            return False
        
        log(f"{method} {path} -> Status: {response.status_code}")
        return response.status_code
    except Exception as e:
        log(f"Connection failed to {url}: {e}", "FAIL")
        return None

def main():
    log("Starting API Consistency Verification...")
    
    # 1. Check Auth Prefix
    log("--- Checking Auth Prefix ---")
    s1 = check_endpoint("GET", "/captcha")
    s2 = check_endpoint("GET", "/api/captcha")
    
    if s1 == 200:
        log("Auth/Captcha is at root (/captcha)", "WARN")
    if s2 == 200:
        log("Auth/Captcha is at /api (/api/captcha)", "PASS")
    
    # 2. Check Technician Method
    log("--- Checking Technician Update Method ---")
    # Assuming user 1 exists, otherwise likely 401 or 404, but 405 Method Not Allowed is what we look for
    s_put = check_endpoint("PUT", "/api/technicians/1/status")
    s_patch = check_endpoint("PATCH", "/api/technicians/1/status")
    
    if s_put == 405:
        log("PUT method correctly rejected (405)", "PASS")
    else:
        log(f"PUT method returned {s_put}, expected 405", "WARN")
        
    if s_patch != 405 and s_patch is not None:
         # 401 or 200 or 400 means endpoint exists and accepts PATCH
         log("PATCH method accepted (endpoint exists)", "PASS")
    
    # 3. Check Room Prefix
    log("--- Checking Room Prefix ---")
    check_endpoint("GET", "/rooms")
    check_endpoint("GET", "/api/rooms")

    log("Verification Complete. See consistency_report.md for details.")

if __name__ == "__main__":
    main()
