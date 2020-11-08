from django.contrib import admin
from .models import Projeto,Post,Categoria

# Register your models here.
class ProjetoAdmin(admin.ModelAdmin):
    fields = ('id', 'titulo', 'link',)
    list_display = ('id', 'titulo', 'link',)
    list_display_links = ('id',)
    list_editable = ('titulo', 'link',)


class PostAdmin(admin.ModelAdmin):
    list_display = ('id', 'titulo', 'categoria', 'data', 'autor', 'publicado',)
    list_display_links = ('id',)
    list_editable = ('titulo', 'categoria', 'data', 'autor', 'publicado',)


class CategoriaAdmin(admin.ModelAdmin):
    pass
admin.site.register(Projeto, ProjetoAdmin)
admin.site.register(Post, PostAdmin)
admin.site.register(Categoria, CategoriaAdmin)