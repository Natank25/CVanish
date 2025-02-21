from flask import Flask, g, request, make_response, render_template
from redis import Redis
import os
import socket
import random
import json

hostname = socket.gethostname()

app = Flask(__name__)

def get_redis():
    if not hasattr(g, 'redis'):
        g.redis = Redis(host=os.environ['REDIS_HOST'], db=0, socket_timeout=5)
    return g.redis

@app.route("/", methods=['POST', 'GET'])
def hello():
    choice_id = request.cookies.get('choice_id')
    if not choice_id:
        choice_id = hex(random.getrandbits(64))[2:-1]
        choice = None
        if request.method == 'POST':
            redis = get_redis()
            choice = request.form['choice']
            data = json.dumps({'choice_id': choice_id, 'choice': choice})
            redis.rpush('choices', data)
        resp = make_response(render_template(
            'sites_offres.html'))
        return resp

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=80, debug=True, threaded=True)