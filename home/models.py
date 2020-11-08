from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
from ckeditor.fields import RichTextField
# Create your models here.
class Projeto(models.Model):
    titulo = models.CharField(max_length=255)
    descricao = models.TextField()
    link = models.CharField(max_length=255)
    image = models.ImageField(upload_to='media', blank=True, default='white_image.jpg')

    def __str__(self):
        return self.titulo

class Categoria(models.Model):
    nome_cat = models.CharField(max_length=255)
    def __str__(self):
        return self.nome_cat

class Post(models.Model):
    titulo = models.CharField(max_length=255)
    conteudo = RichTextField()
    resumo = models.CharField(default="Clique para acessar", blank=True, null=True, max_length=100)
    image = models.ImageField(upload_to='blog', blank=True, default='/blog/white_image.jpg', null=True)
    categoria = models.ForeignKey(Categoria, on_delete=models.DO_NOTHING, blank=True, null=True)
    data = models.DateTimeField(default=timezone.now)
    autor = models.ForeignKey(User, on_delete=models.DO_NOTHING)
    publicado = models.BooleanField(default=False)

    def __str__(self):
        return self.titulo
