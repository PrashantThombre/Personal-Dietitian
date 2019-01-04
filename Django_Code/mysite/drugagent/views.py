# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.http import HttpResponse
from django.shortcuts import render

from django.views.decorators.csrf import csrf_exempt, csrf_protect

# Simple get response.
@csrf_protect
def index(request):
    return HttpResponse("Hello! Welcome to your food administrator...")