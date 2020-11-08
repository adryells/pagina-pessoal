from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from . import views

urlpatterns = [
    path('', views.index, name='index'),
    path('projetos/', views.projetos, name='projetos'),
    path('blog/', views.posts, name='blog'),
    path('blog/<int:post_id>/', views.post, name='post'),
]

