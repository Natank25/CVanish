FROM python
WORKDIR /app
COPY . .
RUN pip install -r requirements.txt
EXPOSE 80
CMD ["flask", "--debug", "run", "--host=0.0.0.0", "--port=80"]
