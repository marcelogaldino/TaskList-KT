import java.util.Date
import java.util.IllegalFormatException
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Task @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val createdAt: Date
)

sealed class Result {
    data class Success(val data: String): Result()
    data class Error(val exception: Exception): Result()
}

interface TaskInterface<Task> {
    fun add(task: Task)
    fun listAll(): List<Task>
    @OptIn(ExperimentalUuidApi::class)
    fun findOne(id: Uuid): Task?
    fun updateStatus(task: Task): Boolean
    @OptIn(ExperimentalUuidApi::class)
    fun delete(id: Uuid): Boolean
    fun filterBy(isCompleted: Boolean): List<Task>
}

class TaskManager: TaskInterface<Task> {
    private val tasks = mutableListOf<Task>()

    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun generateUniqueId(): Uuid {
            return Uuid.random()
        }

        @OptIn(ExperimentalUuidApi::class)
        fun parseUuid(id: String): Uuid {
            return Uuid.parse(id)
        }
    }

    override fun add(task: Task) {
        tasks.add(task)
    }

    override fun listAll(): List<Task> {
        return tasks.toList()
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun findOne(id: Uuid): Task? {
        return tasks.find { it.id == id }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun updateStatus(task: Task): Boolean {
        val isRemoved = tasks.removeIf {it.id == task.id}
        if (isRemoved) return tasks.add(task)
        return false
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun delete(id: Uuid): Boolean {
        return tasks.removeIf { it.id == id }
    }

    override fun filterBy(isCompleted: Boolean): List<Task> {
        return tasks.filter { it.isCompleted == isCompleted }
    }
}

fun validateTitle(title: String?) {
    requireNotNull(title) { "Title must not be null" }
    require(title.isNotEmpty()) { "Title must not be empty" }
}

@OptIn(ExperimentalUuidApi::class)
fun addTask(): Task {
    var title: String? = null
    println("Insira o titulo da task: ")
    while (title == null) {
        print("-> ")
        title = readlnOrNull()
    }
    try {
        validateTitle(title)
    } catch (e: IllegalFormatException) {
        println("ERRO: ${e.message}")
    }
    println("(OPCIONAL) Insira uma descrição para a task: ")
    print("-> ")
    val description: String? = readlnOrNull()

    return Task(
        id = TaskManager.generateUniqueId(),
        title = title,
        description = description,
        isCompleted = false,
        createdAt = Date()
    )
}

@OptIn(ExperimentalUuidApi::class)
fun updateTask(task: Task): Task {
    var isCompletedInput: String? = null

    println("Digite [Y] para task realizada ou [N] para não realizada: ")
    while (isCompletedInput == null) {
        print("-> ")
        isCompletedInput = readlnOrNull()
    }

    val isCompleted: Boolean = isCompletedInput == "Y"

    return Task(
        id = task.id,
        title = task.title,
        description = task.description,
        isCompleted = isCompleted,
        createdAt = task.createdAt
    )
}

fun handleResult(result: Result) {
   println(
       when(result) {
            is Result.Error -> "Houve um erro! ${result.exception.message}"
            is Result.Success -> "Sucesso! ${result.data}"
       }
   )
}

enum class AcoesMenu {
    DESCONHECIDA,
    ADICIONAR_TAREFA,
    ATUALIZAR_STATUS,
    DELETAR_TAREFA,
    BUSCAR_TAREFA,
    SAIR
}

@OptIn(ExperimentalUuidApi::class)
fun main() {
    val taskManager = TaskManager()

    var acao: Int? = 0
    while (acao != AcoesMenu.SAIR.ordinal) {
        // https://tableconvert.com/ascii-generator
        println(
            """
        +------------------------------------+
        |  GERENCIADOR DE TAREFAS - TASKLIST |
        +------------------------------------+
        |  1 - Adicionar                     |
        |  2 - Atualizar                     |
        |  3 - Deletar                       |
        |  4 - Buscar                        |
        |  5 - Sair                          |
        +------------------------------------+
        """
        )
        println("LISTA ATUAL DE TAREFAS:")
        println(taskManager.listAll().joinToString(
            separator = "\n"
        ).ifEmpty { "Nenhuma tarefa foi adicionada até o momento" })

        println("\nInsira a ação do gerenciador de tarefas:")
        print("-> ")
        acao = readlnOrNull()?.toIntOrNull()

        when (acao) {
            AcoesMenu.ADICIONAR_TAREFA.ordinal -> {
                val newTask = addTask()
                taskManager.add(newTask)
                handleResult(Result.Success("Tarefa criada!"))
            }
            AcoesMenu.ATUALIZAR_STATUS.ordinal -> {
                var id: String? = null
                println("Insira o ID do produto a ser atualizado: ")
                while (id == null) {
                    println("-> ")
                    id = readlnOrNull()
                    if(id == null) {
                        try {
                            throw Exception("O ID inserido é inválido. Tente novamente")
                        } catch (e: Exception) {
                            handleResult(Result.Error(e))
                        }
                    }
                }
                val task = taskManager.findOne(id = TaskManager.parseUuid(id)) ?: return println("O Produto não existe")
                val updatedTask = updateTask(task = task)
                taskManager.updateStatus(task = updatedTask)
                handleResult(Result.Success("Status atualizado!"))
            }
            AcoesMenu.DELETAR_TAREFA.ordinal -> {
                var id: String? = null
                println("Insira o ID do produto a ser deletado: ")
                while (id == null) {
                    println("-> ")
                    id = readlnOrNull()
                    if(id == null) {
                        try {
                            throw Exception("O ID inserido é inválido. Tente novamente")
                        } catch (e: Exception) {
                            handleResult(Result.Error(e))
                        }
                    }
                }
                val deletedTask = taskManager.delete(id = TaskManager.parseUuid(id))
                println(
                    if(deletedTask) handleResult(Result.Success("Tarefa deletada"))
                    else "Não existe tarefa com o ID $id"
                )
            }
            AcoesMenu.BUSCAR_TAREFA.ordinal -> {
                var id: String? = null
                println("Insira o ID do produto a ser buscado: ")
                while (id == null) {
                    println("-> ")
                    id = readlnOrNull()
                    if(id == null) {
                        try {
                            throw Exception("O ID inserido é inválido. Tente novamente")
                        } catch (e: Exception) {
                            handleResult(Result.Error(e))
                        }
                    }
                }

                val findedTask = taskManager.findOne(id = TaskManager.parseUuid(id))
                println(
                    if(findedTask != null) handleResult(Result.Success("Tarefa encontrada!! \n Tarefa -> $findedTask"))
                    else "Não existe tarefa com o ID $id"
                )
            }
            AcoesMenu.SAIR.ordinal -> {
                println("Obrigado, volte sempre!")
            }
            else -> println("A opção escolhida é inválida!")
        }
    }
}