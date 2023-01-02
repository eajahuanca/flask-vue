FROM python:3.9

WORKDIR /app

COPY . .

RUN apt-get update && apt-get install -y python3-pip

COPY requirements.txt ./

RUN pip3 install -r requirements.txt

EXPOSE 5000

CMD [ "flask", "run", "--host", "0.0.0.0" ]
