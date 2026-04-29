FROM python:3.14-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir torch torchvision --index-url https://download.pytorch.org/whl/cpu
RUN pip install --no-cache-dir -r requirements.txt
RUN python -c "from rembg import new_session; new_session('u2net')"
COPY . .
EXPOSE 5000
ENV PYTHONPATH=/app
CMD ["fastapi", "run", "app/main.py", "--port", "5000"]