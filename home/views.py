from django.shortcuts import render, get_list_or_404, get_object_or_404
from django.views.generic.list import ListView
from django.views.generic.edit import UpdateView
from .models import Projeto, Post
# Create your views here.
def index(request):
    return render(request, 'home/index.html')

def projetos(request):
    return render(request, 'home/projetos.html', {'projetos': get_list_or_404(Projeto)})

def posts(request):
    return render(request, 'home/posts.html', {'posts': get_list_or_404(Post)})

def post(request, post_id):
    return render(request, 'home/post.html', {'post': get_object_or_404(Post,pk=post_id)})
