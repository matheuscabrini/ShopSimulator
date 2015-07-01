Autoria:
Matheus de França Cabrini - NºUSP: 8937375
Rodrigo de Andrade Santos Weigert - NºUSP: 8937503

Este projeto é um simulador de loja online. É composto pelo programa servidor, onde gerencia-se o estoque de produtos e as compras (que podem estar sendo feitas por múltiplos clientes simultaneamente), além do programa cliente, onde pode-se realizar compras. Foi feito como projeto final para a disciplina SSC0103 - Programação Orientada a Objetos, da Universidade de São Paulo. Mais especificações do projeto estão em: https://goo.gl/rzkrQR

========================
     Modo de execução
========================
        O projeto consiste de duas aplicações: servidor e cliente. Ambos possuem interface gráfica. 
        Para iniciar a aplicação servidor, execute server.jar com o seguinte comando (Windows ou Linux):
            java -jar server.jar <port>
        Analogamente, para o cliente:
            java -jar client.jar <IP do servidor> <port do servidor>
        Também foram disponiblizados, no diretório principal do projeto, arquivos .bat (Windows) e .sh (Linux) 
    para fácil execução dos programas. Basta editá-los com os argumentos desejados. Por padrão, os mesmos inicializam o servidor sobre o IP local (localhost), na porta 6673, e o cliente se conecta a esses mesmos IP e porta.
        Vale notar que para o programa cliente executar corretamente, o mesmo deve ser inicializado de forma a se conectar com um servidor já aberto e funcional.
    
============================
     Modo de uso - Servidor
============================
    A interface é composta por um menu no canto esquerdo-superior e pelas abas com tabelas de produtos, usuários e requisições
cadastradas no sistema. A requisição representa o pedido do usuário por notificação via email quando o estoque de um 
produto, previamente indisponível, for atualizado no servidor.
    No menu "File", pode-se salvar os dados do sistema nos arquivos .csv, ou terminar o programa (ação esta que também
salva automaticamente os dados).
    No menu "Actions", é possível adicionar novo produto no sistema, reestocar determinado produto (por código), e atualizar
as tabelas (refresh). As ações relacionadas aos produtos prontificam o usuário a digitar os dados necessários em janelas separadas. O refresh é feito automaticamente após criar ou alterar produtos, porém deve ser realizado manualmente a fim de se 
observar as compras/requisições feitas pelos clientes, pois estas podem estar ocorrendo a qualquer momento. Portanto, após realizar compras, requisições ou cadastro de novo usuário na aplicação cliente, é necessário dar o refresh no servidor para que as tabelas mostrem corretamente os novos dados.
    Além disso, quando altera-se o estoque de um produto, pode haver uma certo "lag" no programa. Isso ocorre quando há requisições por aquele produto. Logo, tal demora deve-se ao processo de envio do email aos clientes interessados. O email utilizado pelo programa é: "noreply.shop.bot@gmail.com".

============================
     Modo de uso - Cliente
============================
    Primeiramente, na interface, uma notificação de sucesso na conexão com o servidor deve aparecer. Então, o usuário pode escolher digitar seus dados de login (ID e senha) ou criar um novo cadastro, cujos dados serão requisitados pelo programa.
    Tendo seu login validado, entra-se na tela da loja, composta por menu no canto esquerdo-superior, além de duas abas: uma com      a lista de produtos oferecidos pela loja, a outra com a lista de compras presentes no carrinho de compras do usuário. A lista de produtos é automaticamente recebida do servidor.
    Para adicionar compras ao carrinho, dirija-se ao menu "Actions". A adição de produtos é feita pelo código de produto, além da quantidade desejada. Se tal quantidade for incompatível com o atual estoque no programa, o usuário terá a opoturnidade deixar a requisição para que seja notificado via email quando o reestoque do produto no servidor ocorrer.
    As compras/requisições devem ser confirmadas também no menu "Actions". Após serem confirmadas, o servidor deve recebê-las e processá-las no sistema. Se, no intervalo de tempo passado entre as compras, a confirmação, e o recebimento das mesmas pelo servidor, ocorrer indisponibilidade de algum dos produtos comprados, é enviado ao cliente uma lista de produtos cuja transação não foi completada. Também será possível deixar requisições por tais produtos, se houverem. Estas serão enviadas instantaneamente ao servidor.
    No menu "System", é possível requerer do servidor a lista de produtos mais atual, efetivamente causando um refresh na tabela de produtos; além disso, também pode-se terminar o programa.

========================
     Requerimentos
========================

    Este software foi desenvolvido em Java e requer a versão 8u40 (8 update 40) ou superior
do mesmo para funcionar em qualquer sistema operacional.

========================
     Implementação
========================

 Este projeto foi realizado utilizando-se a IDE Eclipse, versão Luna Service Release 2 (4.4.2).
O software é construído em Java 8 e sua interface gráfica é feita com JavaFX 8.
Algumas APIs são utilizadas na implementação de certas funções do programa, nomeadamente:

    -OpenCSV 3.3 para manipulação dos arquivos CSV relativos ao mercado

    -JavaMail 1.5.4 para envio de emails de notificação de reestoques de produtos

Tais APIs (arquivos .jar) estão localizadas na pasta "lib".

Na pasta "CSVs", encontram-se arquivos .csv com os dados dos registros do sistema (produtos, usuários e requisições).
Tais dados são recuperados ao iniciar a aplicação servidor, e os arquivos são atualizados sempre, automaticamente,
ao término do programa.

Além disso, o software também faz uso de alguns padrões de projeto conhecidos:

    -Iterator para iterar nas listas internas da classe ShoppingCart

    -Singleton na classe ShopManager, pois é esperado que um programa lide com apenas um sistema de mercado por vez (não faz sentido lidar com mais de um)
Observer nas classes Product (classe observada) e Requisition (classe observadora). Quando o estoque de um produto é reposto, o produto notifica as requisições que o observam. Estas se encarregam de mandar email para o usuário dono da requisição avisando-o da ocorrência do reestoque.
