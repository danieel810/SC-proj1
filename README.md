# Segurança e Confiabilidade

Projeto de Segurança e Confiabilidadeda Faculdade de Ciências 3 ano

# Como correr

TODO

# Testar

Após o código compilado 

Para correr o servidor: java SeiTchizServer.java 45678

Para correr o cliente: java SeiTchiz.java 127.0.0.1:45678 Daniel

Métodos:

follow <userID> -> o userID pode não existir, caso isso aconteça o cliente é notificado

unfollow <userID> -> o userID pode não existir, caso isso aconteça o cliente é notificado

post <photo> -> o photo é suposto ser o path para a foto ou apenas o nome da foto se tiverem na mesma diretoria

wall <nPhotos> -> o nPhotos tem de ser um inteiro obrigatóriamente

like <photoID> -> o photoID tem de ser do tipo User:ID que é o que é devolvido quando é feito o wall

addu <userID> <groupID> -> caso o userID ou o groupID não existam o cliente é notificado ou caso o cliente não faça parte desse grupo

removeu <userID> <groupID> -> caso o userID ou o groupID não existam o cliente é notificado ou caso o cliente não faça parte desse grupo

msg <groupID> <msg> -> Caso o groupID não exista o cliente é notificado, a menssagem pode ter espaços ou seja do tipo: "ola somos o grupo XX"

collect <groupID> -> Caso o groupID não exista o cliente é notificado

history <groupID> -> Caso o groupID não exista o cliente é notificado

O Trabalho está dividido da seguinte forma:
Um ficheiro para cada utilizador que tem a seguinte informação
User:Daniel
Seguidores:
Seguindo:
Fotos:
ID:0
Grupos:ola/0/0,
Owner:ola,
o ID é o ID da foto atual, quando for posta uma novo foto fica com ID 1 e o ID incrementa
Na secção dos grupos, cada grupo tem um nome (ola) o ID da ultima mensagem que deu collect e o ID da mensagem de quando entrou no grupo
Os users estão todos agrupados numa pasta users

Um ficheiro para cada grupo que tem a seguinte informação
Owner:Daniel
Members:
ID:0
Chat:
Onde o ID é o ID da próxima mensagem

Os grupos estão todos agrupados numa pasta grupos

Cada foto tem o seguinte nome: User:ID
E estão agrupadas numa posta fotos