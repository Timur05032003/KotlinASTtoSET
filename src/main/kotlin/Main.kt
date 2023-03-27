fun main() {
    val root = toExecTree(fooBarAst)
    println(prettyPrint(root))
}

val fooBarAst = Block(
    Let(Var("x"), Const(1)),
    Let(Var("y"), Const(0)),
    If(
        NEq(Var("a"), Const(0)),
        Block(
            Let(Var("y"), Plus(Const(3), Var("x"))),
            If(
                Eq(Var("b"), Const(0)),
                Let(Var("x"), Mul(Const(2), Plus(Var("a"), Var("b")))),
            )
        )
    ),
    Minus(Var("x"), Var("y"))
)

fun toExecTree(expr: Expr): ExecTreeNode {
    val s = mutableListOf<Expr>()
    val pi = mutableListOf<Expr>()
    val root = ExecTreeNode(emptyList(), expr, s, pi)
    val queue = ArrayDeque<ExecTreeNode>()
    queue.add(root)
    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        when (val e = node.nextExpr) {
            is Block -> {
                val children = e.exprs.map { ExecTreeNode(emptyList(), it, s, pi) }
                queue.addAll(children)
                node.children = children
            }

            is Const, is Var -> {
                node.S = s
            }

            is Let -> {
                s.add(e)
                node.S = s
                val child = ExecTreeNode(emptyList(), e.value, s, pi)
                node.children = listOf(child)
                queue.add(child)
            }

            is Eq -> {
                pi.add(e)
                node.Pi = pi

                val leftChild = ExecTreeNode(emptyList(), e.left, s, pi)
                val rightChild = ExecTreeNode(emptyList(), e.right, s, pi)

                node.children = listOf(leftChild, rightChild)
                queue.addAll(listOf(leftChild, rightChild))
            }

            is NEq -> {
                pi.add(e)
                node.Pi = pi

                val leftChild = ExecTreeNode(emptyList(), e.left, s, pi)
                val rightChild = ExecTreeNode(emptyList(), e.right, s, pi)

                node.children = listOf(leftChild, rightChild)
                queue.addAll(listOf(leftChild, rightChild))
            }

            is If -> {
                val condNode = ExecTreeNode(emptyList(), e.cond, s, pi)
                queue.add(condNode)
                val thenNode = ExecTreeNode(emptyList(), e.thenExpr, s, pi + condNode.Pi)
                queue.add(thenNode)

                val elseNode =
                    e.elseExpr?.let { ExecTreeNode(emptyList(), it, s, pi + NEq(e.cond, Const(0))) }
                if (elseNode != null) {
                    queue.add(elseNode)
                    node.children = listOf(condNode, thenNode, elseNode)
                } else {
                    node.children = listOf(condNode, thenNode)
                }
            }

            is Plus -> {
                node.children = listOf(
                    ExecTreeNode(emptyList(), e.left, s.toList(), pi.toList()),
                    ExecTreeNode(emptyList(), e.right, s.toList(), pi.toList())
                )
                queue.addAll(node.children)
            }

            is Minus -> {
                node.children = listOf(
                    ExecTreeNode(emptyList(), e.left, s.toList(), pi.toList()),
                    ExecTreeNode(emptyList(), e.right, s.toList(), pi.toList())
                )
                queue.addAll(node.children)
            }

            is Mul -> {
                node.children = listOf(
                    ExecTreeNode(emptyList(), e.left, s.toList(), pi.toList()),
                    ExecTreeNode(emptyList(), e.right, s.toList(), pi.toList())
                )
                queue.addAll(node.children)
            }

            else -> {
                error("Unknown expression: $e")
            }
        }
    }
    return root
}


fun prettyPrint(node: ExecTreeNode, indent: Int = 0): String {
    val sb = StringBuilder()
    val indentStr = " ".repeat(indent)
    when (node.nextExpr) {
        is Block -> {
            sb.append(indentStr).append("{\n")
            for (child in node.children) {
                sb.append(prettyPrint(child, indent + 2))
            }
            sb.append(indentStr).append("}\n")
        }
        is Let -> {
            sb.append(indentStr).append("let ${node.nextExpr.variable.name} = ")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append("\n")
        }
        is Const -> {
            sb.append(node.nextExpr.value)
        }
        is Var -> {
            sb.append(node.nextExpr.name)
        }
        is Eq -> {
            sb.append("(")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append(" == ")
            sb.append(prettyPrint(node.children[1], 0))
            sb.append(")")
        }
        is NEq -> {
            sb.append("(")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append(" != ")
            sb.append(prettyPrint(node.children[1], 0))
            sb.append(")")
        }
        is If -> {
            sb.append(indentStr).append("if (")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append(") {\n")
            sb.append(prettyPrint(node.children[1], indent + 2))
            sb.append(indentStr).append("}\n")
            if (node.children.size > 2) {
                sb.append(indentStr).append("else {\n")
                sb.append(prettyPrint(node.children[2], indent + 2))
                sb.append(indentStr).append("}\n")
            }
        }
        is Plus -> {
            sb.append(indentStr).append("(")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append(" + ")
            sb.append(prettyPrint(node.children[1], 0))
            sb.append(")")
        }
        is Minus -> {
            sb.append(indentStr).append("(")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append(" - ")
            sb.append(prettyPrint(node.children[1], 0))
            sb.append(")")
        }
        is Mul -> {
            sb.append(indentStr).append("(")
            sb.append(prettyPrint(node.children[0], 0))
            sb.append(" * ")
            sb.append(prettyPrint(node.children[1], 0))
            sb.append(")")
        }
        else -> {
            error("Unknown expression: ${node.nextExpr}")
        }
    }
    return sb.toString()
}

sealed class Expr

class Block(vararg val exprs: Expr) : Expr()

class Const(val value: Int) : Expr()

class Var(val name: String) : Expr()

class Let(val variable: Var, val value: Expr) : Expr()

class Eq(val left: Expr, val right: Expr) : Expr()

class NEq(val left: Expr, val right: Expr) : Expr()

class If(val cond: Expr, val thenExpr: Expr, val elseExpr: Expr? = null) : Expr()

class Plus(val left: Expr, val right: Expr) : Expr()

class Minus(val left: Expr, val right: Expr) : Expr()

class Mul(val left: Expr, val right: Expr) : Expr()

class ExecTreeNode(
    var children: List<ExecTreeNode>,
    val nextExpr: Expr?,
    var S: List<Expr>?,
    var Pi: List<Expr>
)